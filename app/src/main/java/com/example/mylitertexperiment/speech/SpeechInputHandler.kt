package com.example.mylitertexperiment.speech

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SpeechInputHandler {
    val speechCommandState = MutableStateFlow("")
    // TODO: Set up AudioRecord for continuous audio
    // TODO: Run Whisper model via LiteRT Next and update state
} 