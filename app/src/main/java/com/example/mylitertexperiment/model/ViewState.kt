package com.example.mylitertexperiment.model

import android.graphics.Bitmap

// Placeholder DetectedObject import
import android.graphics.Rect
import com.example.mylitertexperiment.model.DetectedObject

data class ViewState(
    val originalFrame: Bitmap?,
    val styledFrame: Bitmap?,
    val detectedObjects: List<DetectedObject>,
    val ocrText: String,
    val styleCommand: String
) {
    companion object {
        fun placeholder() = ViewState(
            originalFrame = null,
            styledFrame = null,
            detectedObjects = emptyList(),
            ocrText = "",
            styleCommand = ""
        )
    }
} 