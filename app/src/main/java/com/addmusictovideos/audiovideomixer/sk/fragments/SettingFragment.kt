package com.addmusictovideos.audiovideomixer.sk.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.addmusictovideos.audiovideomixer.sk.R
import com.addmusictovideos.audiovideomixer.sk.databinding.FragmentSettingBinding
import com.addmusictovideos.audiovideomixer.sk.utils.ShareHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
        binding.ShareApps.setOnClickListener {
            ShareHelper.shareAppLink(requireContext())
        }


        binding.llHome.setOnClickListener {
            val action = SettingFragmentDirections.actionSettingsToNavHome()
            navController.navigate(action)
        }

        binding.llRateUs.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=${requireContext().packageName}")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    )
                )
            }
        }


        binding.llFeedback.setOnClickListener {
            ShareHelper.sendFeedback(requireContext())
        }

        binding.llAdsConsent.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Ads Consent",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.llPrivacyPolicy.setOnClickListener {

            val privacyPolicyUrl =
                "https://sites.google.com/view/privacy-policy-softkeysinc"
            ShareHelper.openPrivacyPolicy(requireContext(), privacyPolicyUrl)
        }


        binding.llMoreApps.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=YourDeveloperName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
            }
        }


    }

    override fun onResume() {
        super.onResume()
    }


}