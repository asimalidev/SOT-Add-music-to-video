package com.addmusictovideos.audiovideomixer.sk.utils

import android.view.View
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentMergeVideoBinding
import com.addmusictovideos.audiovideomixer.sk.model.Video

class MergeVideoHelper {

    fun setupExoPlayer(
        binding: FragmentMergeVideoBinding,
        player: Player,
        selectedVideos: List<Video>,
        mode: String,
        outputPath: String
    ) {
        val mediaItems = mutableListOf<MediaItem>()
        mediaItems.add(MediaItem.fromUri(outputPath))

        selectedVideos.forEach { video ->
            mediaItems.add(MediaItem.fromUri(video.path))
        }

        when (mode) {
            "sequential" -> {
                binding.playerView.visibility = View.VISIBLE
                binding.playerView.player = player
                player.setMediaItems(mediaItems)
                player.prepare()
                player.play()

                player.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        if (state == Player.STATE_ENDED) {
                            val currentIndex = player.currentAdGroupIndex
                            if (currentIndex < mediaItems.size - 1) {
                                player.seekTo(currentIndex + 1, 0)
                                player.play()
                            }
                        }
                    }
                })
            }
        }
    }
}