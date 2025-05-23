package com.addmusictovideos.audiovideomixer.sk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.util.LongSparseArray
import android.view.View
import com.addmusictovideos.audiovideomixer.sk.R
import kotlin.math.ceil

class TimeLineView @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    private var mVideoUri: Uri? = null
    private var mHeightView: Int = 0
    private var mBitmapList: LongSparseArray<Bitmap>? = null

    init {
        init()
    }

    private fun init() {
        mHeightView = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        val w = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + mHeightView
        val h = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(w, h)
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (w != oldW) getBitmap(w)
    }


    private fun getBitmap(viewWidth: Int) {
        BackgroundExecutor.execute(object : BackgroundExecutor.Task("", 0L, "") {
            override fun execute() {
                try {
                    val threshold = 11
                    val thumbnailList = LongSparseArray<Bitmap>()
                    val mediaMetadataRetriever = MediaMetadataRetriever()

                    try {
                        Log.d("TimeLineView", "Video URI: $mVideoUri")
                        mediaMetadataRetriever.setDataSource(context, mVideoUri)

                        val videoLengthInMs = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            ?.toLongOrNull()?.times(1000) ?: 0L
                        if (videoLengthInMs == 0L) {
                            Log.e("TimeLineView", "Failed to retrieve video duration")
                            return
                        }

                        val frameHeight = (mHeightView * 1.5).toInt()
                        val initialBitmap = mediaMetadataRetriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

                        if (initialBitmap == null) {
                            Log.e("TimeLineView", "Failed to retrieve initial frame from video")
                            return
                        }

                        val frameWidth = ((initialBitmap.width.toFloat() / initialBitmap.height.toFloat()) * frameHeight.toFloat()).toInt()
                        var numThumbs = ceil((viewWidth.toFloat() / frameWidth)).toInt()
                        if (numThumbs < threshold) numThumbs = threshold

                        val cropWidth = viewWidth / threshold
                        val interval = videoLengthInMs / numThumbs

                        for (i in 0 until numThumbs) {
                            var bitmap = mediaMetadataRetriever.getFrameAtTime(i * interval, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            if (bitmap != null) {
                                try {
                                    bitmap = Bitmap.createScaledBitmap(bitmap, frameWidth, frameHeight, false)
                                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, cropWidth, bitmap.height)
                                } catch (e: Exception) {
                                    Log.e("TimeLineView", "Error processing bitmap", e)
                                }
                                thumbnailList.put(i.toLong(), bitmap)
                            } else {
                                Log.w("TimeLineView", "Failed to retrieve frame at time: ${i * interval} ms")
                            }
                        }

                        returnBitmaps(thumbnailList)
                    } catch (e: Exception) {
                        Log.e("TimeLineView", "Error retrieving video frames", e)
                    } finally {
                        mediaMetadataRetriever.release()
                    }

                } catch (e: Throwable) {
                    Thread.getDefaultUncaughtExceptionHandler()
                        ?.uncaughtException(Thread.currentThread(), e)
                }
            }
        })
    }


    private fun returnBitmaps(thumbnailList: LongSparseArray<Bitmap>) {
        UiThreadExecutor.runTask("", {
            mBitmapList = thumbnailList
            invalidate()
        }, 0L)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mBitmapList != null) {
            canvas.save()
            var x = 0
            for (i in 0 until (mBitmapList?.size() ?: 0)) {
                val bitmap = mBitmapList?.get(i.toLong())
                if (bitmap != null) {
                    canvas.drawBitmap(bitmap, x.toFloat(), 0f, null)
                    x += bitmap.width
                }
            }
        }
    }

    fun setVideo(data: Uri) {
        mVideoUri = data
    }
}