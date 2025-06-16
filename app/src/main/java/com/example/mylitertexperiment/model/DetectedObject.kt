package com.example.mylitertexperiment.model

import android.graphics.Rect

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val boundingBox: Rect
) 