package com.addmusictovideos.audiovideomixer.sk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Toast

object ShareHelper {
    fun shareAppLink(context: Context) {
        val appPackageName = context.packageName

//        var pkg = "com.whatsapp"

        val playStoreLink =
            "https://play.google.com/store/apps/details?id=$appPackageName"
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_TEXT, playStoreLink)
        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }



    /*fun showRateUsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Rate Us")
        builder.setMessage("If you enjoy using our app, please take a moment to rate it. Thanks for your support!")

        // Set up the rating bar
        val ratingBar = RatingBar(context)
        ratingBar.numStars = 5

        ratingBar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        ratingBar.max = 5
        ratingBar.stepSize = 1.0f // Set step size to 1
        builder.setView(ratingBar)

        // Set up the buttons
        builder.setPositiveButton("Submit") { dialog, which ->
            val rating = ratingBar.rating
            if (rating > 0) {
                // Redirect users to Play Store for rating
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(context, "Unable to find Play Store", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }

        // Display the dialog
        builder.show()
    }*/

    fun showRateUsDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Rate Us")
        builder.setMessage("If you enjoy using our app, please take a moment to rate it. Thanks for your support!")

        // Set up the rating bar
        val ratingBar = RatingBar(context)

        ratingBar.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        ratingBar.max = 5
        ratingBar.stepSize = 1F // Set step size to 1
        ratingBar.numStars = 5 // Set number of stars to 5

        builder.setView(ratingBar)

        // Set up the buttons
        builder.setPositiveButton("Submit") { dialog, which ->
            val rating = ratingBar.rating
            if (rating > 0) {
                // Redirect users to Play Store for rating
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                } catch (e: android.content.ActivityNotFoundException) {
                    Toast.makeText(context, "Unable to find Play Store", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }

        // Display the dialog
        builder.show()
    }


    fun sendFeedback(context: Context) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("your.email@example.com")) // Replace with your email address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback : Add Music to Video") // Subject for the email

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        } catch (ex: android.content.ActivityNotFoundException) {
            // Handle exception if no email app found
            // You may show a toast or dialog informing the user
        }
    }


    fun openPrivacyPolicy(context: Context, privacyPolicyUrl: String) {
        val uri = Uri.parse(privacyPolicyUrl)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle exceptions, if any
        }
    }


}