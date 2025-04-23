package com.addmusictovideos.audiovideomixer.sk.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.annotation.ColorInt
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.TransformationRequest
import androidx.media3.transformer.Transformer
import androidx.media3.ui.AspectRatioFrameLayout
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.TrimmerViewLayoutBinding
import java.util.*

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode

import java.io.File
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.UUID

class VideoEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?=null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    var volumeLevel: Float = 1.0f // Default 100% volume

    private lateinit var mPlayer: ExoPlayer
    private var isSaving = false  // Prevent duplicate execution
    private lateinit var mSrc: Uri
    private var mFinalPath: String? = null
    private var mMaxDuration: Int = -1
    private var mMinDuration: Int = -1
    private var mListeners: ArrayList<OnProgressVideoEvent> = ArrayList()
    private var mOnVideoEditedListener: OnVideoEditedEvent? = null
    private lateinit var binding: TrimmerViewLayoutBinding
    private var mDuration: Long = 0L
    private var mTimeVideo = 0L
    var mStartPosition = 0L
    var mEndPosition = 0L
    private var mResetSeekBar = false
    private val mMessageHandler = MessageHandler(this)
    private var originalVideoWidth: Int = 0
    private var originalVideoHeight: Int = 0
    private var videoPlayerWidth: Int = 0
    private var videoPlayerHeight: Int = 0
    private var bitRate: Int = 2
    private var isVideoPrepared = false
    private var videoPlayerCurrentPosition = 0L

    private var destinationPath: String
        get() {
            if (mFinalPath == null) {
                val folder = context.cacheDir
                mFinalPath = folder.path + File.separator
            }
            return mFinalPath ?: ""
        }
        set(finalPath) {
            mFinalPath = finalPath
        }

    init {
        init(context)
    }

    fun setVolume(level: Float) {
        Log.d("VideoEditor", "Setting volume to $level") // Debug log
        volumeLevel = level
        mPlayer.volume = volumeLevel // Set volume for ExoPlayer
    }

    private fun init(context: Context) {
        binding = TrimmerViewLayoutBinding.inflate(LayoutInflater.from(context), this, true)
        setUpListeners()
        setUpMargins()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListeners() {
        mListeners = ArrayList()
        mListeners.add(object : OnProgressVideoEvent {
            override fun updateProgress(time: Float, max: Long, scale: Long) {
                updateVideoProgress(time.toLong())
            }
        })

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    onClickVideoPlayPause()
                    return true
                }
            })
        binding.iconVideoPlay.setOnClickListener {
            onClickVideoPlayPause()
        }
        binding.layoutSurfaceView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
        binding.handlerTop.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onPlayerIndicatorSeekChanged(progress, fromUser)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStart()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                onPlayerIndicatorSeekStop(seekBar)
            }
        })
        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarEvent {
            override fun onCreate(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {
            }

            override fun onSeek(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {
            }

            override fun onSeekStop(
                rangeSeekBarView: RangeSeekBarView,
                index: Int,
                value: Float
            ) {
                onStopSeekThumbs()
            }
        })
    }

    private fun onPlayerIndicatorSeekChanged(progress: Int, fromUser: Boolean) {
        val duration = (mDuration * progress / 1000L)
        if (fromUser) {
            if (duration < mStartPosition) setProgressBarPosition(mStartPosition)
            else if (duration > mEndPosition) setProgressBarPosition(mEndPosition)
        }
    }

    private fun onPlayerIndicatorSeekStart() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mPlayer.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE
        notifyProgressUpdate(false)
    }

    private fun onPlayerIndicatorSeekStop(seekBar: SeekBar) {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mPlayer.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE

        val duration = (mDuration * seekBar.progress / 1000L).toInt()
        mPlayer.seekTo(duration.toLong())
        notifyProgressUpdate(false)
    }

    private fun setProgressBarPosition(position: Long) {
        if (mDuration > 0) binding.handlerTop.progress = (1000L * position / mDuration).toInt()
    }

    private fun setUpMargins() {
        val marge = binding.timeLineBar.thumbs[0].widthBitmap
        val lp = binding.timeLineView.layoutParams as LayoutParams
        lp.setMargins(marge, 0, marge, 0)
        binding.timeLineView.layoutParams = lp
    }

    private fun onClickVideoPlayPause() {
        if (mPlayer.isPlaying) {
            binding.iconVideoPlay.visibility = View.VISIBLE
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mPlayer.pause()
        } else {
            binding.iconVideoPlay.visibility = View.GONE
            if (mResetSeekBar) {
                mResetSeekBar = false
                mPlayer.seekTo(mStartPosition)
            }
            mResetSeekBar = false
            mMessageHandler.sendEmptyMessage(SHOW_PROGRESS)
            mPlayer.play()
        }
    }

    private fun onVideoPrepared(mp: ExoPlayer) {
        if (isVideoPrepared) return
        isVideoPrepared = true
        val videoWidth = mp.videoSize.width
        val videoHeight = mp.videoSize.height
        val videoProportion = videoWidth.toFloat() / videoHeight.toFloat()
        val screenWidth = binding.layoutSurfaceView.width
        val screenHeight = binding.layoutSurfaceView.height
        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()
        val lp = binding.videoLoader.layoutParams

        if (videoProportion > screenProportion) {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() / videoProportion).toInt()
        } else {
            lp.width = (videoProportion * screenHeight.toFloat()).toInt()
            lp.height = screenHeight
        }
        videoPlayerWidth = lp.width
        videoPlayerHeight = lp.height
        binding.videoLoader.layoutParams = lp

        binding.iconVideoPlay.visibility = View.VISIBLE
        mDuration = mPlayer.duration
        setSeekBarPosition()
        setTimeFrames()
    }

    private fun setSeekBarPosition() {
        when {
            mDuration >= mMaxDuration && mMaxDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMaxDuration / 2
                mEndPosition = mDuration / 2 + mMaxDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }

            mDuration <= mMinDuration && mMinDuration != -1 -> {
                mStartPosition = mDuration / 2 - mMinDuration / 2
                mEndPosition = mDuration / 2 + mMinDuration / 2
                binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration))
                binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration))
            }

            else -> {
                mStartPosition = 0L
                mEndPosition = mDuration
            }
        }
        mPlayer.seekTo(mStartPosition)
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
    }

    private fun setTimeFrames() {
        val seconds = context.getString(R.string.short_seconds)
        binding.textTimeSelection.text = String.format(
            Locale.ENGLISH,
            "%s %s - %s %s",
            TrimVideoUtils.stringForTime(mStartPosition),
            seconds,
            TrimVideoUtils.stringForTime(mEndPosition),
            seconds
        )
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            Thumb.LEFT -> {
                mStartPosition = ((mDuration * value / 100L).toLong())
                mPlayer.seekTo(mStartPosition)
            }

            Thumb.RIGHT -> {
                mEndPosition = ((mDuration * value / 100L).toLong())
            }
        }
        setTimeFrames()
        mTimeVideo = mEndPosition - mStartPosition
    }

    private fun onStopSeekThumbs() {
        mMessageHandler.removeMessages(SHOW_PROGRESS)
        mPlayer.pause()
        binding.iconVideoPlay.visibility = View.VISIBLE
    }

    private fun notifyProgressUpdate(all: Boolean) {
        if (mDuration == 0L) return
        val position = mPlayer.currentPosition
        if (all) {
            for (item in mListeners) {
                item.updateProgress(position.toFloat(), mDuration, (position * 100 / mDuration))
            }
        } else {
            mListeners[0].updateProgress(
                position.toFloat(),
                mDuration,
                (position * 100 / mDuration)
            )
        }
    }

    private fun updateVideoProgress(time: Long) {
        if (binding.videoLoader == null) return
        if (time <= mStartPosition && time <= mEndPosition) binding.handlerTop.visibility =
            View.GONE
        else binding.handlerTop.visibility = View.VISIBLE
        if (time >= mEndPosition) {
            mMessageHandler.removeMessages(SHOW_PROGRESS)
            mPlayer.pause()
            binding.iconVideoPlay.visibility = View.VISIBLE
            mResetSeekBar = true
            return
        }
        setProgressBarPosition(time)
    }

    fun setVideoBackgroundColor(@ColorInt color: Int) = with(binding) {
        container.setBackgroundColor(color)
        layout.setBackgroundColor(color)
    }

    fun releasePlayer() {
        if (::mPlayer.isInitialized) {
            mPlayer.pause()
            mPlayer.stop()
            mPlayer.release()
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun saveVideo(volumeLevel: Float) {
        val txtTime = binding.textTimeSelection.text.toString()
        val pattern = "\\d{2}:\\d{2}"
        val regex = Regex(pattern)
        val matches = regex.findAll(txtTime)
        val timeList = matches.map { it.value }.toList()

        val timeStamp = System.currentTimeMillis()
        val trimmedFilePath = "$destinationPath/${timeStamp}_trimmed.mp4"
        val outputFilePath = "$destinationPath/${timeStamp}_final.mp4"

        val startMilliseconds = timeToMilliseconds(timeList[0])
        val endMilliseconds = timeToMilliseconds(timeList[1])

        val transformation = TransformationRequest.Builder()
            .setVideoMimeType(MimeTypes.VIDEO_H264)
            .setAudioMimeType(MimeTypes.AUDIO_AAC)
            .build()

        val transformer = Transformer.Builder(context)
            .setTransformationRequest(transformation)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    adjustVolumeWithFFmpeg(trimmedFilePath, outputFilePath, volumeLevel)
                    isSaving = false
                }

                override fun onError(composition: Composition, exportResult: ExportResult, exportException: ExportException) {
                    exportException.localizedMessage?.let { mOnVideoEditedListener?.onError(it) }
                    isSaving = false
                }
            })
            .build()

        val inputMediaItem = MediaItem.Builder()
            .setUri(mSrc)
            .setClippingConfiguration(
                MediaItem.ClippingConfiguration.Builder()
                    .setStartPositionMs(startMilliseconds)
                    .setEndPositionMs(endMilliseconds)
                    .build()
            )
            .build()

        val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
            .setFrameRate(30)
            .setEffects(Effects.EMPTY)
            .build()

        val composition = Composition.Builder(listOf(EditedMediaItemSequence(listOf(editedMediaItem)))).build()

        transformer.start(composition, trimmedFilePath)
    }

    private fun adjustVolumeWithFFmpeg(inputPath: String, outputPath: String, volume: Float) {
        val cmd = arrayOf(
            "-y",
            "-i", "\"$inputPath\"",
            "-filter:a", "volume=$volume",
            "-c:v", "copy",
            outputPath
        )

        getDuration(inputPath) { totalDuration ->
            FFmpegKit.executeAsync(cmd.joinToString(" "), { session ->
                val state = session.state
                val returnCode = session.returnCode

                context?.let { ctx ->
                    if (ReturnCode.isSuccess(returnCode)) {
                        Log.e("CheckingSuccess", "Success")
                        if (File(inputPath).exists()) {
                            File(inputPath).delete()
                        }
                        mOnVideoEditedListener?.getResult(Uri.parse(outputPath))
                    } else {
                        mOnVideoEditedListener?.onError("Failed to adjust volume")
                        Log.e("CheckingElse", String.format("FFMpeg process exited with state %s and rc %s.%s", state, returnCode, session.failStackTrace))
                    }
                }
            }, { log ->
                Log.d("Checking", "1: $log")
            }, { statistics ->

            })
        }
    }
    private fun getDuration(inputPath: String, callback: (Long) -> Unit) {
        FFprobeKit.getMediaInformationAsync(inputPath) { session ->
            val mediaInformation = session.mediaInformation
            val durationString = mediaInformation.duration
            val duration = durationString?.toDoubleOrNull() ?: 0.0
            callback((duration * 1000).toLong())
        }
    }

    private fun timeToMilliseconds(time: String): Long {
        val parts = time.split(":")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid time format: $time")
        }
        val minutes = parts[0].toInt()
        val seconds = parts[1].trim().split(" ")[0].toInt() // Extract seconds
        return ((minutes * 60 + seconds) * 1000).toLong()
    }

    fun setVideoInformationVisibility(visible: Boolean): VideoEditor {
        binding.timeFrame.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    fun setOnTrimVideoListener(onVideoEditedListener: OnVideoEditedEvent): VideoEditor {
        mOnVideoEditedListener = onVideoEditedListener
        return this
    }

    fun setDestinationPath(path: String): VideoEditor {
        destinationPath = path
        return this
    }

    fun setMaxDuration(maxDuration: Int): VideoEditor {
        mMaxDuration = maxDuration * 1000
        return this
    }

    fun setMinDuration(minDuration: Int): VideoEditor {
        mMinDuration = minDuration * 1000
        return this
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun setVideoURI(videoURI: Uri): VideoEditor {
        mSrc = videoURI
        /**setup video player**/
        mPlayer = ExoPlayer.Builder(context).build()
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val videoSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoURI))
        mPlayer.setMediaSource(videoSource)
        mPlayer.prepare()
        mPlayer.playWhenReady = false
        binding.videoLoader.also {
            it.player = mPlayer
            it.useController = false
        }
        mPlayer.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                mOnVideoEditedListener?.onError("Something went wrong reason : ${error.localizedMessage}")
            }

            @SuppressLint("UnsafeOptInUsageError")
            override fun onVideoSizeChanged(videoSize: VideoSize) {
                super.onVideoSizeChanged(videoSize)
                if (mPlayer.videoSize.width > mPlayer.videoSize.height) {
                    binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                } else {
                    binding.videoLoader.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                    mPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                }
                onVideoPrepared(mPlayer)
            }
        })
        binding.videoLoader.requestFocus()
        binding.timeLineView.setVideo(mSrc)
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(context, mSrc)
        val metaDateWidth =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toInt() ?: 0
        val metaDataHeight =
            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toInt() ?: 0
        //If the rotation is 90 or 270 the width and height will be transposed.
        when (mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            ?.toInt()) {
            90, 270 -> {
                originalVideoWidth = metaDataHeight
                originalVideoHeight = metaDateWidth
            }

            else -> {
                originalVideoWidth = metaDateWidth
                originalVideoHeight = metaDataHeight
            }
        }
        return this
    }

    private class MessageHandler(view: VideoEditor) : Handler(Looper.getMainLooper()) {
        private val mView: WeakReference<VideoEditor> = WeakReference(view)
        override fun handleMessage(msg: Message) {
            val view = mView.get()
            if (view == null || view.binding.videoLoader == null) return
            view.notifyProgressUpdate(true)
            if (view.binding.videoLoader.player?.isPlaying == true) sendEmptyMessageDelayed(0, 10)
        }
    }

    companion object {
        private const val MIN_TIME_FRAME = 1000
        private const val SHOW_PROGRESS = 2
        private const val MIN_BITRATE = 1500000.0
    }
}

private fun Double.bitToMb() = this / 1000000