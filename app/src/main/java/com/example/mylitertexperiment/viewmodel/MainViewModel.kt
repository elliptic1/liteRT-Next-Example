package com.example.mylitertexperiment.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylitertexperiment.model.ViewState
import com.example.mylitertexperiment.model.DetectedObject
import com.example.mylitertexperiment.repository.ModelRepository
import kotlinx.coroutines.flow.*

class MainViewModel(
    context: Context,
) : ViewModel() {
    private val repository: ModelRepository = ModelRepository(context)
    val errorFlow: SharedFlow<String> = repository.errorFlow
    val uiState: StateFlow<ViewState> = combine(
        repository.cameraFrameFlow,
        repository.styledFrameFlow,
        repository.classificationFlow,
        repository.ocrFlow,
        repository.speechCommandState
    ) {
            frame: android.graphics.Bitmap,
            styled: android.graphics.Bitmap,
            objects: List<DetectedObject>,
            text: String,
            command: String,
        ->
        Log.d("ModelOutput", "new viewstate: ${frame.width}x${frame.height}, styled: ${styled.width}x${styled.height}, objects: ${objects.size}, ocrText: $text, command: $command")
        ViewState(
            originalFrame = frame,
            styledFrame = styled,
            detectedObjects = objects,
            ocrText = text,
            styleCommand = command
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ViewState.placeholder())
} 