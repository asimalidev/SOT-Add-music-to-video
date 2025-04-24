package com.addmusictovideos.audiovideomixer.sk.utils

import android.app.Application
import com.addmusictovideos.audiovideomixer.sk.R
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

open class ApplicationClass: Application() {

    companion object {
        lateinit var applicationClass: ApplicationClass
    }

    override fun onCreate() {
        super.onCreate()
        applicationClass = this
        MobileAds.initialize(this)
        FirebaseApp.initializeApp(applicationContext)
        val crashlyticsEnabled = resources.getString(R.string.CrashlyticsEnabled).toBoolean()
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(crashlyticsEnabled)
    }
}