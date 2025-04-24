package com.addmusictovideos.audiovideomixer.sk.utils

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.addmusictovideos.audiovideomixer.sk.MainActivity
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.ActivitySotSplashBinding
import com.addmusictovideos.audiovideomixer.sk.utils.ApplicationClass.Companion.applicationClass
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.manual.mediation.library.sotadlib.adMobAdClasses.AdMobBannerAdSplash
import com.manual.mediation.library.sotadlib.callingClasses.LanguageScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsConfigurations
import com.manual.mediation.library.sotadlib.callingClasses.SOTAdsManager
import com.manual.mediation.library.sotadlib.callingClasses.WalkThroughScreensConfiguration
import com.manual.mediation.library.sotadlib.callingClasses.WelcomeScreensConfiguration
import com.manual.mediation.library.sotadlib.data.Language
import com.manual.mediation.library.sotadlib.data.WalkThroughItem
import com.manual.mediation.library.sotadlib.metaAdClasses.MetaBannerAdSplash
import com.manual.mediation.library.sotadlib.mintegralAdClasses.MintegralBannerAdSplash
import com.manual.mediation.library.sotadlib.unityAdClasses.UnityBannerAdSplash
import com.manual.mediation.library.sotadlib.utils.MyLocaleHelper
import com.manual.mediation.library.sotadlib.utils.NetworkCheck
import com.manual.mediation.library.sotadlib.utils.PrefHelper
import com.manual.mediation.library.sotadlib.utilsGoogleAdsConsent.ConsentConfigurations

class SOT_SplashActivity : AppCompatActivity() {
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig? = null
    private lateinit var sotAdsConfigurations: SOTAdsConfigurations
    private var firstOpenFlowAdIds: HashMap<String, String> = HashMap()
    private var isDuplicateScreenStarted = true

    private lateinit var binding: ActivitySotSplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySotSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSystemUI()
        supportActionBar?.hide()
       // nativeAdMobHashMap?.clear()
        //collapsibleBannerAdMobHashMap?.clear()
        Glide.with(this@SOT_SplashActivity)
            .asBitmap()
            .load(R.drawable.appicon)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .skipMemoryCache(true)
            .into(binding.imageView)


        startFirstOpenFlow()
    }
    private fun startFirstOpenFlow() {
        CustomFirebaseEvents.logEvent(this,eventName = "sot_adlib_start_scr")
        firstOpenFlowAdIds.apply {
            this["ADMOB_SPLASH_INTERSTITIAL"] = resources.getString(R.string.ADMOB_SPLASH_INTERSTITIAL)
            this["ADMOB_SPLASH_RESUME"] = resources.getString(R.string.ADMOB_SPLASH_RESUME)
            this["ADMOB_BANNER_SPLASH"] = resources.getString(R.string.ADMOB_BANNER_SPLASH)
            this["ADMOB_NATIVE_LANGUAGE_1"] = resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_1)
            this["ADMOB_NATIVE_LANGUAGE_2"] = resources.getString(R.string.ADMOB_NATIVE_LANGUAGE_2)
            this["ADMOB_NATIVE_SURVEY_1"] = resources.getString(R.string.ADMOB_NATIVE_SURVEY_1)
            this["ADMOB_NATIVE_SURVEY_2"] = resources.getString(R.string.ADMOB_NATIVE_SURVEY_2)
            this["ADMOB_NATIVE_WALKTHROUGH_1"] = resources.getString(R.string.ADMOB_NATIVE_WALKTHROUGH_1)
            this["ADMOB_NATIVE_WALKTHROUGH_2"] = resources.getString(R.string.ADMOB_NATIVE_WALKTHROUGH_2)
            this["ADMOB_NATIVE_WALKTHROUGH_FULLSCR"] = resources.getString(R.string.ADMOB_NATIVE_WALKTHROUGH_FULLSCR)
            this["ADMOB_NATIVE_WALKTHROUGH_3"] = resources.getString(R.string.ADMOB_NATIVE_WALKTHROUGH_3)
            this["ADMOB_INTERSTITIAL_LETS_START"] = resources.getString(R.string.ADMOB_INTERSTITIAL_LETS_START)

            this["META_SPLASH_INTERSTITIAL"] = resources.getString(R.string.META_SPLASH_INTERSTITIAL)
            this["META_SPLASH_RESUME"] = resources.getString(R.string.META_SPLASH_RESUME)
            this["META_BANNER_SPLASH"] = resources.getString(R.string.META_BANNER_SPLASH)
            this["META_NATIVE_LANGUAGE_1"] = resources.getString(R.string.META_NATIVE_LANGUAGE_1)
            this["META_NATIVE_LANGUAGE_2"] = resources.getString(R.string.META_NATIVE_LANGUAGE_2)
            this["META_NATIVE_SURVEY_1"] = resources.getString(R.string.META_NATIVE_SURVEY_1)
            this["META_NATIVE_SURVEY_2"] = resources.getString(R.string.META_NATIVE_SURVEY_2)
            this["META_NATIVE_WALKTHROUGH_1"] = resources.getString(R.string.META_NATIVE_WALKTHROUGH_1)
            this["META_NATIVE_WALKTHROUGH_2"] = resources.getString(R.string.META_NATIVE_WALKTHROUGH_2)
            this["META_NATIVE_WALKTHROUGH_FULLSCR"] = resources.getString(R.string.META_NATIVE_WALKTHROUGH_FULLSCR)
            this["META_NATIVE_WALKTHROUGH_3"] = resources.getString(R.string.META_NATIVE_WALKTHROUGH_3)
            this["META_INTERSTITIAL_LETS_START"] = resources.getString(R.string.META_INTERSTITIAL_LETS_START)

            // Ad PlacementID-UnitID
            this["MINTEGRAL_SPLASH_INTERSTITIAL"] = resources.getString(R.string.MINTEGRAL_SPLASH_INTERSTITIAL)
            this["MINTEGRAL_SPLASH_RESUME"] = resources.getString(R.string.MINTEGRAL_SPLASH_RESUME)
            this["MINTEGRAL_BANNER_SPLASH"] = resources.getString(R.string.MINTEGRAL_BANNER_SPLASH)
            this["MINTEGRAL_BANNER_LANGUAGE_1"] = resources.getString(R.string.MINTEGRAL_BANNER_LANGUAGE_1)
            this["MINTEGRAL_BANNER_LANGUAGE_2"] = resources.getString(R.string.MINTEGRAL_BANNER_LANGUAGE_2)
            this["MINTEGRAL_BANNER_SURVEY_1"] = resources.getString(R.string.MINTEGRAL_BANNER_SURVEY_1)
            this["MINTEGRAL_BANNER_SURVEY_2"] = resources.getString(R.string.MINTEGRAL_BANNER_SURVEY_2)
            this["MINTEGRAL_BANNER_WALKTHROUGH_1"] = resources.getString(R.string.MINTEGRAL_BANNER_WALKTHROUGH_1)
            this["MINTEGRAL_BANNER_WALKTHROUGH_2"] = resources.getString(R.string.MINTEGRAL_BANNER_WALKTHROUGH_2)
            this["MINTEGRAL_BANNER_WALKTHROUGH_FULLSCR"] = resources.getString(R.string.MINTEGRAL_BANNER_WALKTHROUGH_FULLSCR)
            this["MINTEGRAL_BANNER_WALKTHROUGH_3"] = resources.getString(R.string.MINTEGRAL_BANNER_WALKTHROUGH_3)
            this["MINTEGRAL_INTERSTITIAL_LETS_START"] = resources.getString(R.string.MINTEGRAL_INTERSTITIAL_LETS_START)
        }

        SOTAdsManager.setOnFlowStateListener(

            reConfigureBuilders = {
                Log.d("KAleem", "startFirstOpenFlow: ")
                SOTAdsManager.refreshStrings(setUpWelcomeScreen(this), getWalkThroughList(this))
            },
            onFinish = {
                CustomFirebaseEvents.logEvent(this,eventName = "sot_adlib_end_scr")
                gotoMainActivity()
            }
        )

        val consentConfig = ConsentConfigurations.Builder()
            .setApplicationContext(applicationClass)
            .setMintegralInitializationId(appId = resources.getString(R.string.MINTEGRAL_APP_ID), appKey = resources.getString(R.string.MINTEGRAL_APP_KEY))
            .setUnityInitializationId(gameId = "", testMode = false)
            .setActivityContext(this)
            .setTestDeviceHashedIdList(arrayListOf(
                "09DD12A6DB3BBF9B55D65FAA9FD7D8E0",
                "3F8FB4EE64D851EDBA704E705EC63A62",
                "84C3994693FB491110A5A4AEF8C5561B",
                "CB2F3812ACAA2A3D8C0B31682E1473EB",
                "F02B044F22C917805C3DF6E99D3B8800"))
            .setOnConsentGatheredCallback {
                Log.i("ConsentMessage","SOTStartActivity: setOnConsentGatheredCallback")
                fetchAdIDS(
                    remoteConfigOperationsCompleted = {
                        sotAdsConfigurations.setRemoteConfigData(
                            activityContext = this@SOT_SplashActivity,
                            myRemoteConfigData = it)

                        if (NetworkCheck.isNetworkAvailable(this)) {
                            if (it.getValue(RemoteConfigConst.BANNER_SPLASH) == true) {
                                binding.bannerAd.visibility = View.VISIBLE
                                when {
                                    it.getValue(RemoteConfigConst.BANNER_SPLASH_MED) == "ADMOB" -> {
                                        loadAdmobBannerAd()
                                    }
                                    it.getValue(RemoteConfigConst.BANNER_SPLASH_MED) == "META" -> {
                                        loadMetaBannerAd()
                                    }
                                    it.getValue(RemoteConfigConst.BANNER_SPLASH_MED) == "MINTEGRAL" -> {
                                        loadMintegralBannerAd()
                                    }
                                    it.getValue(RemoteConfigConst.BANNER_SPLASH_MED) == "UNITY" -> {
                                        loadUnityBannerAd()
                                    }
                                }
                            }
                        }
                    }
                )
            }
            .build()

        val welcomeScreensConfiguration = WelcomeScreensConfiguration.Builder()
            .setActivityContext(this)
            .setXMLLayout(setUpWelcomeScreen(this))
            .build()

        val languageScreensConfiguration = LanguageScreensConfiguration.Builder()
            .setActivityContext(this)
            .setDrawableColors(
                selectedDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_lang_selected)!!,
                unSelectedDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_lang_unselected)!!,
                selectedRadio = AppCompatResources.getDrawable(this, R.drawable.selected_radio)!!,
                unSelectedRadio = AppCompatResources.getDrawable(this, R.drawable.unselected_radio)!!
            )
            .setLanguages(arrayListOf(Language.Urdu, Language.English, Language.Hindi, Language.French, Language.Dutch, Language.Arabic, Language.German))
            .build()

        val walkThroughScreensConfiguration = WalkThroughScreensConfiguration.Builder()
            .setActivityContext(this)
            .setWalkThroughContent(getWalkThroughList(this))
            .build()

        sotAdsConfigurations = SOTAdsConfigurations.Builder()
            .setFirstOpenFlowAdIds(firstOpenFlowAdIds)
            .setConsentConfig(consentConfig)
            .setLanguageScreenConfiguration(languageScreensConfiguration)
            .setWelcomeScreenConfiguration(welcomeScreensConfiguration)
            .setWalkThroughScreenConfiguration(walkThroughScreensConfiguration)
            .build()

        SOTAdsManager.startFlow(sotAdsConfigurations)
    }


    private fun setUpWelcomeScreen(context: Context): View {
        Log.d("KAleem", "setUpWelcomeScreen: ")
        val localizedConfig = resources.configuration.apply { MyLocaleHelper.onAttach(context, "en") }
        val localizedContext = ContextWrapper(this).createConfigurationContext(localizedConfig)

        val welcomeScreenView = LayoutInflater.from(localizedContext).inflate(R.layout.layout_welcome_scr_1,null,false)
        val progressAnim = welcomeScreenView.findViewById<LottieAnimationView>(R.id.progress)

        val txtWallpapers = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtWallpapers)
        val txtEditor = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtEditor)
        val txtLiveThemes = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtLiveThemes)
        val txtPhotoOnKeyboard = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoOnKeyboard)
        val txtPhotoTranslator = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtPhotoTranslator)
        val txtInstantSticker = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtInstantSticker)

        var txtWallpapersBool = false
        var txtEditorBool = false
        var txtLiveThemesBool = false
        var txtPhotoOnKeyboardBool = false
        var txtPhotoTranslatorBool = false
        var txtInstantStickerBool = false

        val nextButton = welcomeScreenView.findViewById<AppCompatTextView>(R.id.txtNext)

        txtWallpapers.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_wallpaper")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtWallpapersBool) {
                txtWallpapersBool = false
                txtWallpapers.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtWallpapersBool = true
                txtWallpapers.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtEditor.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_pashto_editor")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtEditorBool) {
                txtEditorBool = false
                txtEditor.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtEditorBool = true
                txtEditor.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtLiveThemes.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_live_themes")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtLiveThemesBool) {
                txtLiveThemesBool = false
                txtLiveThemes.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtLiveThemesBool = true
                txtLiveThemes.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtPhotoOnKeyboard.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_photo_on_keyboard")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtPhotoOnKeyboardBool) {
                txtPhotoOnKeyboardBool = false
                txtPhotoOnKeyboard.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtPhotoOnKeyboardBool = true
                txtPhotoOnKeyboard.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtPhotoTranslator.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_photo_translator")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtPhotoTranslatorBool) {
                txtPhotoTranslatorBool = false
                txtPhotoTranslator.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtPhotoTranslatorBool = true
                txtPhotoTranslator.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }
        txtInstantSticker.setOnClickListener {
            CustomFirebaseEvents.logEvent(this,eventName = "survey_scr_check_instant_stickers")
            if (isDuplicateScreenStarted) {
                SOTAdsManager.showWelcomeDupScreen()
            }
            isDuplicateScreenStarted = false
            progressAnim.visibility = View.GONE
            if (txtInstantStickerBool) {
                txtInstantStickerBool = false
                txtInstantSticker.setBackgroundResource(R.drawable.ic_unselected_state)
            } else {
                txtInstantStickerBool = true
                txtInstantSticker.setBackgroundResource(R.drawable.ic_selected_state)
            }
        }

        nextButton.setOnClickListener {
            if (txtWallpapersBool || txtEditorBool ||
                txtLiveThemesBool || txtPhotoOnKeyboardBool ||
                txtPhotoTranslatorBool || txtInstantStickerBool) {
                CustomFirebaseEvents.logEvent(this,eventName = "survey2_scr")
                CustomFirebaseEvents.logEvent(this,eventName = "survey2_scr_tap_continue")
                SOTAdsManager.completeWelcomeScreens()
            } else {
                CustomFirebaseEvents.logEvent(this,eventName = "survey1_scr")
                CustomFirebaseEvents.logEvent(this,eventName = "survey1_scr_tap_continue")
                if (isDuplicateScreenStarted) {
                    SOTAdsManager.showWelcomeDupScreen()
                }
                isDuplicateScreenStarted = false
                val toast = Toast.makeText(this, "Please check the checkbox", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER,0,0)
                toast.show()
            }
        }
        return welcomeScreenView
    }


    private fun getWalkThroughList(context: Context): ArrayList<WalkThroughItem> {
        Log.d("KAleem", "getWalkThroughList: ")
        val localizedContext = ContextWrapper(this).createConfigurationContext(
            resources.configuration.apply { MyLocaleHelper.onAttach(context, "en") }
        )
        return arrayListOf(
            WalkThroughItem(
                heading = localizedContext.getString(R.string.add_music_to_video),
                description = localizedContext.getString(R.string.add_music_to_video_with_multiple_background_tracks),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.black,
                drawable = AppCompatResources.getDrawable(context, R.drawable.ic_w_1),
                drawableBubble = AppCompatResources.getDrawable(context, R.drawable.ic_bubble_one)
            ),
            WalkThroughItem(
                heading = localizedContext.getString(R.string.change_video_speed),
                description = localizedContext.getString(R.string.you_can_change_the_speed_for_video_fast_or_slow_motion_control),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.black,
                drawable = AppCompatResources.getDrawable(context, R.drawable.ic_w_2),
                drawableBubble = AppCompatResources.getDrawable(context, R.drawable.ic_bubble_two)
            ),
            WalkThroughItem(
                heading = localizedContext.getString(R.string.video_cutter_and_editor),
                description = localizedContext.getString(R.string.cut_video_merge_video_split_video_in_timeline_or_multi_split_videos_into_several_clips),
                headingColor = R.color.black,
                descriptionColor = R.color.black,
                nextColor = R.color.black,
                drawable = AppCompatResources.getDrawable(context, R.drawable.ic_w_3),
                drawableBubble = AppCompatResources.getDrawable(context, R.drawable.ic_bubble_three)
            )
        )
    }

    private fun fetchAdIDS(remoteConfigOperationsCompleted: (HashMap<String, Any>) -> Unit) {
        if (NetworkCheck.isNetworkAvailable(this@SOT_SplashActivity)) {
            mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0).build()
            mFirebaseRemoteConfig!!.setConfigSettingsAsync(configSettings)
            mFirebaseRemoteConfig!!.fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mFirebaseRemoteConfig!!.activate()
                    saveAllValues()
                }
                remoteConfigOperationsCompleted.invoke(getSharedPreferencesValues())
            }
        } else {
            remoteConfigOperationsCompleted.invoke(getSharedPreferencesValues())
        }
    }

    private fun saveAllValues() {
        val editor = getSharedPreferences("RemoteConfig", MODE_PRIVATE).edit()
        // SOT-Ads-Visibility-Config
        mFirebaseRemoteConfig?.apply {
            getString(RemoteConfigConst.RESUME_INTER_SPLASH).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.RESUME_INTER_SPLASH, it)
            }
            editor.putBoolean(RemoteConfigConst.BANNER_SPLASH, getBoolean(RemoteConfigConst.BANNER_SPLASH))
            editor.putBoolean(RemoteConfigConst.RESUME_OVERALL, getBoolean(RemoteConfigConst.RESUME_OVERALL))
            editor.putBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1, getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1))
            editor.putBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2, getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2))
            editor.putBoolean(RemoteConfigConst.NATIVE_SURVEY_1, getBoolean(RemoteConfigConst.NATIVE_SURVEY_1))
            editor.putBoolean(RemoteConfigConst.NATIVE_SURVEY_2, getBoolean(RemoteConfigConst.NATIVE_SURVEY_2))
            editor.putBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1, getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1))
            editor.putBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2, getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2))
            editor.putBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR, getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR))
            editor.putBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3, getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3))
            editor.putBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START, getBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START))

            // SOT-Ads-Mediation-Config
            getString(RemoteConfigConst.RESUME_INTER_SPLASH_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.RESUME_INTER_SPLASH_MED, it)
            }
            getString(RemoteConfigConst.RESUME_OVERALL_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.RESUME_OVERALL_MED, it)
            }
            getString(RemoteConfigConst.BANNER_SPLASH_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.BANNER_SPLASH_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_LANGUAGE_1_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_LANGUAGE_1_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_LANGUAGE_2_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_LANGUAGE_2_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_SURVEY_1_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_SURVEY_1_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_SURVEY_2_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_SURVEY_2_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_WALKTHROUGH_1_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_WALKTHROUGH_1_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_WALKTHROUGH_2_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_WALKTHROUGH_2_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR_MED, it)
            }
            getString(RemoteConfigConst.NATIVE_WALKTHROUGH_3_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.NATIVE_WALKTHROUGH_3_MED, it)
            }

            getString(RemoteConfigConst.INTERSTITIAL_LETS_START_MED).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.INTERSTITIAL_LETS_START_MED, it)
            }

            getString(RemoteConfigConst.OVERALL_BANNER_RELOADING).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.OVERALL_BANNER_RELOADING, it)
            }
            getString(RemoteConfigConst.OVERALL_NATIVE_RELOADING).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.OVERALL_NATIVE_RELOADING, it)
            }

            getString(RemoteConfigConst.TIMER_NATIVE_F_SRC).trim().takeIf { it.isNotEmpty() }?.let {
                editor.putString(RemoteConfigConst.TIMER_NATIVE_F_SRC, it)
            }
        }

        editor.apply()
        saveAllValuesForInsideAppAds()
    }

    private fun saveAllValuesForInsideAppAds() {

        /*if (!TextUtils.isEmpty(mFirebaseRemoteConfig!!.getString(INTERSTITIAL_OPEN_PHOTO).trim())) {
            getSharedPreferences("RemoteConfig", MODE_PRIVATE)
                .edit().putString(INTERSTITIAL_OPEN_PHOTO,
                    mFirebaseRemoteConfig!!.getString(INTERSTITIAL_OPEN_PHOTO))
                .apply()
        }*/


    }

    private fun getSharedPreferencesValues(): HashMap<String, Any> {
        val remoteConfigHashMap: HashMap<String, Any> = HashMap()
        val prefs: SharedPreferences = getSharedPreferences("RemoteConfig", Context.MODE_PRIVATE)

        remoteConfigHashMap.apply {
            this["RESUME_INTER_SPLASH"] = "${prefs.getString(RemoteConfigConst.RESUME_INTER_SPLASH, "Empty")}"
            this["BANNER_SPLASH"] = prefs.getBoolean(RemoteConfigConst.BANNER_SPLASH, false)
            this["RESUME_OVERALL"] = prefs.getBoolean(RemoteConfigConst.RESUME_OVERALL, false)
            this["NATIVE_LANGUAGE_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_1, false)
            this["NATIVE_LANGUAGE_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_LANGUAGE_2, false)
            this["NATIVE_SURVEY_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_1, false)
            this["NATIVE_SURVEY_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_SURVEY_2, false)
            this["NATIVE_WALKTHROUGH_1"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_1, false)
            this["NATIVE_WALKTHROUGH_2"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_2, false)
            this["NATIVE_WALKTHROUGH_FULLSCR"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR, false)
            this["NATIVE_WALKTHROUGH_3"] = prefs.getBoolean(RemoteConfigConst.NATIVE_WALKTHROUGH_3, false)
            this["INTERSTITIAL_LETS_START"] = prefs.getBoolean(RemoteConfigConst.INTERSTITIAL_LETS_START, false)

            this["RESUME_INTER_SPLASH_MED"] = "${prefs.getString(RemoteConfigConst.RESUME_INTER_SPLASH_MED, "Empty")}"
            this["RESUME_OVERALL_MED"] = "${prefs.getString(RemoteConfigConst.RESUME_OVERALL_MED, "Empty")}"
            this["BANNER_SPLASH_MED"] = "${prefs.getString(RemoteConfigConst.BANNER_SPLASH_MED, "Empty")}"
            this["NATIVE_LANGUAGE_1_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_LANGUAGE_1_MED, "Empty")}"
            this["NATIVE_LANGUAGE_2_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_LANGUAGE_2_MED, "Empty")}"
            this["NATIVE_SURVEY_1_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_SURVEY_1_MED, "Empty")}"
            this["NATIVE_SURVEY_2_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_SURVEY_2_MED, "Empty")}"
            this["NATIVE_WALKTHROUGH_1_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_WALKTHROUGH_1_MED, "Empty")}"
            this["NATIVE_WALKTHROUGH_2_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_WALKTHROUGH_2_MED, "Empty")}"
            this["NATIVE_WALKTHROUGH_FULLSCR_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_WALKTHROUGH_FULLSCR_MED, "Empty")}"
            this["NATIVE_WALKTHROUGH_3_MED"] = "${prefs.getString(RemoteConfigConst.NATIVE_WALKTHROUGH_3_MED, "Empty")}"
            this["INTERSTITIAL_LETS_START_MED"] = "${prefs.getString(RemoteConfigConst.INTERSTITIAL_LETS_START_MED, "Empty")}"

            this["TIMER_NATIVE_F_SRC"] = "${prefs.getString(RemoteConfigConst.TIMER_NATIVE_F_SRC, "Empty")}"
        }
        return remoteConfigHashMap
    }

    private fun loadAdmobBannerAd() {
        AdMobBannerAdSplash(this@SOT_SplashActivity,
            placementID = resources.getString(R.string.ADMOB_BANNER_SPLASH),
            bannerContainer = binding.bannerAd,
            shimmerContainer = binding.bannerShimmerLayout.bannerShimmerParent ,
            onAdFailed = {
                binding.bannerAd.visibility = View.GONE
            },
            onAdLoaded = {
            },
            onAdClicked = {}
        )
    }

    private fun loadMetaBannerAd() {
        MetaBannerAdSplash(this@SOT_SplashActivity,
            placementID = resources.getString(R.string.META_BANNER_SPLASH),
            bannerContainer = binding.bannerAd,
            shimmerContainer = binding.bannerShimmerLayout.bannerShimmerParent,
            onAdFailed = {
                binding.bannerAd.visibility = View.GONE
            },
            onAdLoaded = {
            },
            onAdClicked = {}
        )
    }
    private fun gotoMainActivity() {
        Log.d("KAleem", "gotoMainActivity: ")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        /*val time = if (PrefHelper(this).getBooleanDefault("StartScreens", default = false)) { 0 } else { if (NetworkCheck.isNetworkAvailable(this)) { 0 } else { 3000 } }
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SOT_SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, time.toLong())*/
    }
    private fun loadMintegralBannerAd() {
        if (resources.getString(R.string.MINTEGRAL_BANNER_SPLASH).split("-").size == 2) {
            MintegralBannerAdSplash(
                activity = this@SOT_SplashActivity,
                placementID = resources.getString(R.string.MINTEGRAL_BANNER_SPLASH).split("-")[0],
                unitID = resources.getString(R.string.MINTEGRAL_BANNER_SPLASH).split("-")[1],
                bannerContainer = binding.bannerAd,
                shimmerContainer = binding.bannerShimmerLayout.bannerShimmerParent,
                onAdFailed = {
                    binding.bannerAd.visibility = View.GONE
                },
                onAdLoaded = {
                },
                onAdClicked = {}
            )
        } else {
            Log.e("SOT_ADS_TAG","BANNER : Mintegral : SPLASH MAY have Incorrect ID Format (placementID-unitID)")
        }
    }

    private fun loadUnityBannerAd() {
        UnityBannerAdSplash.showBannerAds(
            activity = this,
            bannerContainer = binding.bannerAd,
            placementId = "BANNER_SPLASH")
    }


    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11 and above
            window.setDecorFitsSystemWindows(false)
            val controller = window.insetsController
            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // For Android 10 and below
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}