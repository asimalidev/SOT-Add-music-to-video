package com.addmusictovideos.audiovideomixer.sk.activities

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import com.addmusictovideos.audiovideomixer.sk.utils.HelperClass
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.model.Folder
import com.addmusictovideos.audiovideomixer.sk.model.Video
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList: ArrayList<Folder>
        lateinit var searchList: ArrayList<Video>
        var search: Boolean = false
        var themeIndex: Int = 0
        var sortValue: Int = 0
        val sortList = arrayOf(
            MediaStore.Video.Media.DATE_ADDED + " DESC",
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.TITLE + " DESC",
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.SIZE + " DESC"
        )
    }

    private lateinit var navController: NavController
    private lateinit var toolbar: ConstraintLayout
    private lateinit var settings: ImageView

    val sortList = arrayOf(
        MediaStore.Video.Media.DATE_ADDED + " DESC",
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.TITLE + " DESC",
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.SIZE + " DESC"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        val languageCode = SharedPref.getPrefsInstance().getSelectedLanguageCode()
//        setAppLocale(this, languageCode ?: "en") // Default to English
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.clToolbar)
        settings = findViewById(R.id.ivSettings)
        toolbar.visibility = View.VISIBLE
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        settings.setOnClickListener {
            navController.navigate(R.id.nav_settings)
        }

        requestRuntimePermission()

    }

    fun setAppLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun requestRuntimePermission(): Boolean {
        val permissions = mutableListOf<String>()

        // Android 13+ (API 33+): Request media permissions for videos and audio
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_MEDIA_VIDEO)
            }
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_MEDIA_AUDIO)
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.RECORD_AUDIO)
            }

            // Request POST_NOTIFICATIONS permission (for showing notifications)
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Android 12 and below: Request READ_EXTERNAL_STORAGE
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        // Request WRITE_EXTERNAL_STORAGE for Android <= API 28
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        return if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 13)
            false
        } else {
            true
        }
    }

    override fun onResume() {
        super.onResume()
        HelperClass.hideSystemUI(window)
    }
}