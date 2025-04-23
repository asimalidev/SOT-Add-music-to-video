package com.addmusictovideos.audiovideomixer.sk.utils

import android.content.Context
import android.os.Environment
import java.io.File

class FilePathManager(context: Context) {
    var contextWrapper: Context = context
    val trimAudioPath = "/trim_audio/"
    val trimAudioPathSlideShow = "/trim_audio_slideshow/"
    val parentDirPath = "/VideoTools"
    val converted_to_gif_parent_dir = "/converted_to_gif/"
    val compressed_video_dir = "/compressed_videos/"
    val change_motion_dir = "/change_speed_videos/"
    val fast_motion_dir = "/fast_speed_videos/"
    val slow_motion_dir = "/slow_speed_videos/"
    val video_to_audio_dir = "/video_to_audio/"
    val reverse_video_dir = "/reverse_video/"
    val filter_video_dir = "/filter_video/"
    val trimmed_video_dir = "/trimmed_video/"
    val convert_video_dir = "/convert_video/"
    val convert_to_Mp3_dir = "/convert_video/"
    val change_music_video_dir = "/change_music_video/"
    val temp_trimmed_video_dir = "/.temp_trimmed_video/"
    val merge_video_dir = "/merge_video/"
    val convert_audio_dir = "/convert_audio/"
    val merge_audio_dir = "/merge_audio/"
    val split_audio_dir = "/split_audio/"
    val record_audio_dir = "/record_audio/"
    val mix_audio_dir = "/mix_audio/"
    val speed_audio_dir = "/speed_audio/"
    private val slideShowDir = "/slide_show/"
    private val slideShowDotDir = "/.slide_show/"
    private val slideShowMusicDotDir = "/.slide_show/"

    private fun getParentDir(contextWrapper: Context): String {
        if (!File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + parentDirPath).exists()) {
            File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + parentDirPath).mkdirs()
        }
        return contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)!!.absolutePath + parentDirPath
    }

    fun getConvertVideoFormatsParentDir(): String {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val videoToolsDir = File(moviesDir, parentDirPath)
        val convertedVideosDir = File(videoToolsDir, convert_video_dir)

        if (!convertedVideosDir.exists()) {
            convertedVideosDir.mkdirs()
        }

        return convertedVideosDir.absolutePath
    }

    private fun getParentDirs(contextWrapper: Context): String {
        if (!File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.absolutePath + parentDirPath).exists()) {
            File(contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.absolutePath + parentDirPath).mkdirs()
        }
        return contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!.absolutePath + parentDirPath
    }

    fun getConvertedGifParentDir(): String {
        if (!File(getParentDir(contextWrapper) + converted_to_gif_parent_dir).exists()) {
            File(getParentDir(contextWrapper) + converted_to_gif_parent_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + converted_to_gif_parent_dir

    }

    fun getConvertedAudioDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + convert_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + convert_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + convert_audio_dir

    }

    fun getmergeAudioDirectory(): String {
        if (!File(getParentDirs(contextWrapper) +  merge_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + merge_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + merge_audio_dir

    }

    fun getAudioSpeedDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + speed_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + speed_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + speed_audio_dir

    }

    fun getAudioTrimAudioSlideShowDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + trimAudioPathSlideShow).exists()) {
            File(getParentDirs(contextWrapper) + trimAudioPathSlideShow).mkdirs()
        }
        return getParentDirs(contextWrapper) + trimAudioPathSlideShow

    }

    fun getAudioCutDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + trimAudioPath).exists()) {
            File(getParentDirs(contextWrapper) + trimAudioPath).mkdirs()
        }
        return getParentDirs(contextWrapper) + trimAudioPath

    }

    fun getAudioMixDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + mix_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + mix_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + mix_audio_dir

    }

    fun getAudioMergeDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + merge_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + merge_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + merge_audio_dir

    }

    fun getAudioSplitDirectory(): String {
        if (!File(getParentDirs(contextWrapper) + split_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + split_audio_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + split_audio_dir

    }

    fun getTrimAudioParentDir(): String {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val videoToolsDir = File(musicDir, parentDirPath)
        val trimAudioDir = File(videoToolsDir, trimAudioPath)

        if (!trimAudioDir.exists()) {
            trimAudioDir.mkdirs()
        }
        return trimAudioDir.absolutePath
    }

    fun getCompressedParentDir(): String {
        if (!File(getParentDir(contextWrapper) + compressed_video_dir).exists()) {
            File(getParentDir(contextWrapper) + compressed_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + compressed_video_dir

    }

    fun getCutAudioParentDir(): String {
        if (!File(getParentDir(contextWrapper) + trimAudioPath).exists()) {
            File(getParentDir(contextWrapper) + trimAudioPath).mkdirs()
        }
        return getParentDir(contextWrapper) + trimAudioPath

    }


    fun getMP3ParentDir(): String {
        if (!File(getParentDirs(contextWrapper) + convert_to_Mp3_dir).exists()) {
            File(getParentDirs(contextWrapper) + convert_to_Mp3_dir).mkdirs()
        }
        return getParentDirs(contextWrapper) + convert_to_Mp3_dir

    }

    fun getSlideshowVideoParentDir(): String {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val videoToolsDir = File(moviesDir, parentDirPath)
        val convertedVideosDir = File(videoToolsDir, slideShowDir)

        if (!convertedVideosDir.exists()) {
            convertedVideosDir.mkdirs()
        }

        return convertedVideosDir.absolutePath
    }

    fun getSlideshowVideoTempParentDir(): String {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val videoToolsDir = File(moviesDir, parentDirPath)
        val convertedVideosDir = File(videoToolsDir, slideShowDotDir)

        if (!convertedVideosDir.exists()) {
            convertedVideosDir.mkdirs()
        }

        return convertedVideosDir.absolutePath
    }

    fun getSlideShowAudiosParentDir(): String {
        val moviesDir = File(contextWrapper.filesDir, parentDirPath)
        val convertedVideosDir = File(moviesDir, slideShowMusicDotDir)

        if (!convertedVideosDir.exists()) {
            convertedVideosDir.mkdirs()
        }

        return convertedVideosDir.absolutePath
    }

    fun getSlowSpeedParentDir(): String {
        if (!File(getParentDir(contextWrapper) + slow_motion_dir).exists()) {
            File(getParentDir(contextWrapper) + slow_motion_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + slow_motion_dir
    }

    fun getFastSpeedParentDir(): String {
        if (!File(getParentDir(contextWrapper) + fast_motion_dir).exists()) {
            File(getParentDir(contextWrapper) + fast_motion_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + fast_motion_dir
    }

    fun getChangeSpeedParentDir(): String {
        if (!File(getParentDir(contextWrapper) + change_motion_dir).exists()) {
            File(getParentDir(contextWrapper) + change_motion_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + change_motion_dir
    }

    fun getVideoToAudioParentDir(): String {
        if (!File(getParentDir(contextWrapper) + video_to_audio_dir).exists()) {
            File(getParentDir(contextWrapper) + video_to_audio_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + video_to_audio_dir
    }

    fun getReverseVideoParentDir(): String {
        if (!File(getParentDir(contextWrapper) + reverse_video_dir).exists()) {
            File(getParentDir(contextWrapper) + reverse_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + reverse_video_dir
    }

    fun getFilterVideoParentDir(): String {
        if (!File(getParentDir(contextWrapper) + filter_video_dir).exists()) {
            File(getParentDir(contextWrapper) + filter_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + filter_video_dir
    }

    fun getTrimmedVideoParentDir(): String {
        if (!File(getParentDir(contextWrapper) + trimmed_video_dir).exists()) {
            File(getParentDir(contextWrapper) + trimmed_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + trimmed_video_dir
    }


    fun getChangeMusicVideoParentDir(): String {
        if (!File(getParentDir(contextWrapper) + change_music_video_dir).exists()) {
            File(getParentDir(contextWrapper) + change_music_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + change_music_video_dir
    }



    fun getMergeVideoParentDir(): String {
        if (!File(getParentDir(contextWrapper) + merge_video_dir).exists()) {
            File(getParentDir(contextWrapper) + merge_video_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + merge_video_dir
    }

    fun getConvertedAudioParentDir(): String {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val videoToolsDir = File(moviesDir, parentDirPath)
        val convertedVideosDir = File(videoToolsDir, convert_audio_dir)

        if (!convertedVideosDir.exists()) {
            convertedVideosDir.mkdirs()
        }

        return convertedVideosDir.absolutePath
    }

    fun getMergeAudioParentDir(): String {
        if (!File(getParentDir(contextWrapper) + merge_audio_dir).exists()) {
            File(getParentDir(contextWrapper) + merge_audio_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + merge_audio_dir
    }

    fun getSplitAudioParentDir(): String {
        if (!File(getParentDir(contextWrapper) + split_audio_dir).exists()) {
            File(getParentDir(contextWrapper) + split_audio_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + split_audio_dir
    }


    fun getRecordAudioParentDir(): String {
        if (!File(getParentDir(contextWrapper) + record_audio_dir).exists()) {
            File(getParentDir(contextWrapper) + record_audio_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + record_audio_dir
    }


     fun getMixAudioParentDir(): String {
        if (!File(getParentDirs(contextWrapper) + mix_audio_dir).exists()) {
            File(getParentDirs(contextWrapper) + mix_audio_dir).mkdirs()
        }
        return getParentDir(contextWrapper) + mix_audio_dir
    }


    //get new file names methods


    fun getNewAudioFileName(): String {
        var string: String
        if (File(getVideoToAudioParentDir()).list() == null) {
            string = getVideoToAudioParentDir() + "audio_" + 0
        } else {
            string =
                getVideoToAudioParentDir() + "audio_" + File(getVideoToAudioParentDir()).list().size

        }

        return string
    }

    fun getNewChangeSpeedFileName(): String {
        if (File(getChangeSpeedParentDir()).list() == null) {
            return getChangeSpeedParentDir() + "change_speed_" + 0
        }
        return getChangeSpeedParentDir() + "change_speed_" + File(getChangeSpeedParentDir()).list().size
    }

    fun getNewCompressedFileName(): String {
        if (File(getCompressedParentDir()).list() == null) {
            return return getCompressedParentDir() + "compress_video_" + 0
        }

        return getCompressedParentDir() + "compress_video_" + File(getCompressedParentDir()).list().size
    }

    fun getNewConvertedGIfFileName(): String {
        if (File(getConvertedGifParentDir()).list() == null) {
            return getConvertedGifParentDir() + "gif_" + 0
        }
        return getConvertedGifParentDir() + "gif_" + File(getConvertedGifParentDir()).list().size
    }

    fun getNewReverseVideoFileName(): String {
        if (File(getReverseVideoParentDir()).list() == null) {
            return getReverseVideoParentDir() + "reverse_video_" + 0
        }
        return getReverseVideoParentDir() + "reverse_video_" + File(getReverseVideoParentDir()).list().size
    }

    fun getNewFilterVideoFileName(): String {
        if (File(getFilterVideoParentDir()).list() == null) {
            return getFilterVideoParentDir() + "filter_video_" + 0
        }

        return getFilterVideoParentDir() + "filter_video_" + File(getFilterVideoParentDir()).list().size
    }

    fun getNewTrimmedVideoFileName(): String {
        if (File(getTrimmedVideoParentDir()).list() == null) {
            return return "trimed_video_" + 0
        }

        return "trimed_video_" + File(getTrimmedVideoParentDir()).list().size
    }

    fun getNewChangeMusicVideoName(): String {
        if (File(getChangeMusicVideoParentDir()).list() == null) {
            return getChangeMusicVideoParentDir() + "change_music_" + 0
        }

        return getChangeMusicVideoParentDir() + "change_music_" + File(getChangeMusicVideoParentDir()).list().size
    }

    fun getNewMergeVideoFileName(): String {
        if (File(getMergeVideoParentDir()).list() == null) {
            return getMergeVideoParentDir() + "merge_video_" + 0
        }

        return getMergeVideoParentDir() + "merge_video_" + File(getMergeVideoParentDir()).list().size
    }

    fun getNewMergeAudioFileName(): String {
        if (File(getMergeAudioParentDir()).list() == null) {
            return getMergeAudioParentDir() + "merge_audio_" + 0
        }

        return getMergeAudioParentDir() + "merge_audio_" + File(getMergeAudioParentDir()).list().size

    }

    fun getNewSplitAudioFileName(): String {
        if (File(getSplitAudioParentDir()).list() == null) {
            return getSplitAudioParentDir() + "split_audio_" + 0
        }
        return getSplitAudioParentDir() + "split_audio_" + File(getSplitAudioParentDir()).list().size
    }

    fun getNewRecordAudioFileName(): String {
        if (File(getRecordAudioParentDir()).list() == null) {
            return "record_audio_" + 0
        }
        return "record_audio_" + File(getRecordAudioParentDir()).list().size
    }

    fun getMixAudioNewFilePath(): String {
        if (File(getMixAudioParentDir()).list() == null) {
            return getMixAudioParentDir() + "mix_audio_" + 0
        }
        return getMixAudioParentDir() + "mix_audio_" + File(getMixAudioParentDir()).list().size
    }

    fun getAudioSpeedFilePath(): String {
        if (File(getMixAudioParentDir()).list() == null) {
            return getMixAudioParentDir() + "audio_speed_" + 0
        }
        return getMixAudioParentDir() + "audio_speed_" + File(getMixAudioParentDir()).list().size
    }
}