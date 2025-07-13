package com.example.mylitertexperiment.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.mylitertexperiment.model.DetectedObject
import com.example.mylitertexperiment.model.ClassificationExecutor
import com.example.mylitertexperiment.model.StyleTransferExecutor
import com.example.mylitertexperiment.model.OCRExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log

class ModelRepository(
    context: Context,
    private val classificationExecutor: ClassificationExecutor = ClassificationExecutor(context),
    private val styleTransferExecutor: StyleTransferExecutor = StyleTransferExecutor(context),
    private val ocrExecutor: OCRExecutor = OCRExecutor(context)
) {
    // Camera frame stream
    val cameraFrameFlow = MutableSharedFlow<Bitmap>()
    // Style transfer output
    val styledFrameFlow = MutableSharedFlow<Bitmap>()
    // Classification results
    val classificationFlow = MutableSharedFlow<List<DetectedObject>>()
    // OCR results
    val ocrFlow = MutableSharedFlow<String>()
    // Speech command
    val speechCommandState = MutableStateFlow("")


    // Aggregated error stream
    val errorFlow = MutableSharedFlow<String>()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // Collect camera frames and run all models in parallel
        scope.launch {
            cameraFrameFlow.collectLatest { frame ->
                // Classification
                launch {
                    classificationExecutor.runAsync(frame)
                }
                // Style transfer
                launch {
                    styleTransferExecutor.runAsync(frame)
                }
                // OCR
//                launch {
//                    ocrExecutor.runAsync(frame)
//                }
            }
        }
        // Collect model outputs and emit to flows
        scope.launch {
            classificationExecutor.outputFlow.collect { result ->
                classificationFlow.emit(result)
            }
        }
        scope.launch {
            styleTransferExecutor.outputFlow.collect { result ->
                styledFrameFlow.emit(result)
            }
        }
        scope.launch {
            classificationExecutor.errorFlow.collect { error ->
                errorFlow.emit(error)
            }
        }
        scope.launch {
            styleTransferExecutor.errorFlow.collect { error ->
                errorFlow.emit(error)
            }
        }
//        scope.launch {
//            ocrExecutor.outputFlow.collect { result ->
//                ocrFlow.emit(result)
//            }
//        }
//        scope.launch {
//            ocrExecutor.errorFlow.collect { error ->
//                errorFlow.emit(error)
//            }
//        }
        // TODO: Load LiteRT Next models in each executor
        // TODO: Handle errors and model lifecycle

        scope.launch {
            cameraFrameFlow.collect { frame ->
//                Log.d("ModelOutput", "cameraFrameFlow: ${frame.width}x${frame.height}")
            }
        }
        scope.launch {
            styledFrameFlow.collect { frame ->
//                Log.d("ModelOutput", "styledFrameFlow: ${frame.width}x${frame.height}")
            }
        }
        scope.launch {
            classificationFlow.collect { result ->
                for (obj in result) {
                    Log.d("ModelOutput", "Detected object: ${obj.label} at ${obj.boundingBox}")
                }
            }
        }
//        scope.launch {
//            ocrFlow.collect { result ->
//                Log.d("ModelOutput", "ocrFlow emitted: $result")
//            }
//        }
        scope.launch {
            speechCommandState.collect { result ->
                Log.d("ModelOutput", "speechCommandState emitted: $result")
            }
        }
    }
} 