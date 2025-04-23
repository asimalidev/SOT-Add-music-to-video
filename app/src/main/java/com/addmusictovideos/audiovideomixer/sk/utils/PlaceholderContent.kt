package com.addmusictovideos.audiovideomixer.sk.utils

import java.util.ArrayList

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<PlaceholderItem> = ArrayList()


//    private val COUNT = 0

//    init {
//        // Add some sample items.
//
//        for (i in 0..COUNT) {
//
//            addItem(createPlaceholderItem(i))
//        }
//    }

//    private fun addItem(item: PlaceholderItem) {
//        ITEMS.add(item)
//    }

//    private fun createPlaceholderItem(position: Int): PlaceholderItem {
//        return PlaceholderItem("Item " + position, makeDetails(position))
//    }

//    private fun makeDetails(position: Int): String {
//        val builder = StringBuilder()
//        builder.append("Details about Item: ").append(position)
//        for (i in 0..position - 1) {
//            builder.append("\nMore details information here.")
//        }
//        return builder.toString()
//    }

    /*
     * A placeholder item representing a piece of content.
     */
    data class PlaceholderItem(val url: String, val name: String) {
        override fun toString(): String = url
    }
}