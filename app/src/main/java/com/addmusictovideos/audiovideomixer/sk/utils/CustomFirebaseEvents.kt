package com.addmusictovideos.audiovideomixer.sk.utils

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object CustomFirebaseEvents {
    lateinit var analytics: FirebaseAnalytics
    lateinit var bundle: Bundle
    lateinit var className: String

    fun init(context: Activity) {
        analytics = FirebaseAnalytics.getInstance(context)
        bundle = Bundle()
        className = context.localClassName
    }

    fun nativeAdEvent(context: Activity) {
        init((context))
        bundle.putString("Native$className", "Native$className")
        analytics.logEvent("Native$className", bundle)
    }

    fun interstitialAdEvent(context: Context, eventName: String) {
        analytics = FirebaseAnalytics.getInstance(context)
        bundle = Bundle()

        bundle.putString("interstitialAd$eventName", "interstitialAd$eventName")
        analytics.logEvent("interstitialAd$eventName", bundle)
    }

    fun logEvent(context: Activity, screenName: String = "", trigger: String? = "null", eventName: String) {
        init(context)
        Log.i("EventTracking", "logEvent: $eventName")
        analytics.logEvent(eventName, null)
    }

    fun nativeKeypadAdEvent(context: Context, eventName: String) {
        analytics = FirebaseAnalytics.getInstance(context)
        bundle = Bundle()

        bundle.putString("KeypadNativeAd$eventName", "Event$eventName")
        analytics.logEvent("KeypadNativeAd$eventName", bundle)
    }

    fun customDialogDismissEvent(context: Context, eventName: String) {
        analytics = FirebaseAnalytics.getInstance(context)
        bundle = Bundle()
        bundle.putString("customDialogDismiss$eventName", "customDialogDismiss$eventName")
        analytics.logEvent("customDialogDismiss$eventName", bundle)
    }

    fun activitiesFragmentEvent(context: Activity, myClassName: String) {
        init((context))
        bundle.putString("Events$myClassName", "Events$myClassName")
        analytics.logEvent(myClassName, bundle)
    }
}