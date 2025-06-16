package com.example.mylitertexperiment.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.TensorBuffer
import androidx.core.graphics.scale

class ClassificationExecutor(val context: Context) : BaseModelExecutor<Bitmap, List<DetectedObject>>() {
    override val outputFlow: SharedFlow<List<DetectedObject>> = MutableSharedFlow()
    private val _outputFlow = outputFlow as MutableSharedFlow<List<DetectedObject>>

    private val compiledModel: CompiledModel = CompiledModel.create(
        context.assets,
        "mobilenet_v3.tflite",
        CompiledModel.Options(Accelerator.CPU) // or Accelerator.GPU
    )
    private val inputBuffers: List<TensorBuffer> = compiledModel.createInputBuffers()
    private val outputBuffers: List<TensorBuffer> = compiledModel.createOutputBuffers().also {
        try {
            val testOutput = it[0].readFloat()
            android.util.Log.d("ClassificationExecutor", "Output buffer initial float array size: ${testOutput.size}")
        } catch (e: Exception) {
            android.util.Log.e("ClassificationExecutor", "Error reading output buffer size after creation: ${e.message}")
        }
    }
    private val inputSize = 224 // MobileNetV3 default
    private val numClasses = 1001
    private val labels = loadLabels(context)

    override suspend fun runAsync(input: Bitmap) {
        withContext(Dispatchers.Default) {
            // Preprocess Bitmap: resize to 224x224, normalize to [0,1], NHWC float32
            val resized = input.scale(inputSize, inputSize)
            val floatArray = bitmapToNormalizedFloatArray(resized)
            android.util.Log.d("ClassificationExecutor", "Input float array size: ${floatArray.size}")
            if (floatArray.size != inputSize * inputSize * 3) {
                android.util.Log.e("ClassificationExecutor", "Float array size mismatch! Expected: ${inputSize * inputSize * 3}, Actual: ${floatArray.size}")
                return@withContext
            }
            inputBuffers[0].writeFloat(floatArray)

            // Run inference with try/catch
            try {
                compiledModel.run(inputBuffers, outputBuffers)
            } catch (e: Exception) {
                android.util.Log.e("ClassificationExecutor", "Exception during model run: ${e.message}")
                return@withContext
            }

            // Postprocess: get top-1 label and confidence
            val output = outputBuffers[0].readFloat()
            android.util.Log.d("ClassificationExecutor", "Output float array size: ${output.size}")
            if (output.size != numClasses) {
                android.util.Log.e("ClassificationExecutor", "Output array size mismatch! Expected: $numClasses, Actual: ${output.size}")
                return@withContext
            }
            val maxIdx = output.indices.maxByOrNull { output[it] } ?: -1
            val confidence = if (maxIdx >= 0) output[maxIdx] else 0f
            val label = labels[maxIdx+1] // or labels[maxIdx + 1] if off by one
            val detected = DetectedObject(label, confidence, Rect(0, 0, input.width, input.height))
            _outputFlow.emit(listOf(detected))
        }
    }

    private fun bitmapToNormalizedFloatArray(bitmap: Bitmap): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val floatArray = FloatArray(width * height * 3)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            floatArray[i * 3 + 0] = ((pixel shr 16 and 0xFF) / 255.0f)
            floatArray[i * 3 + 1] = ((pixel shr 8 and 0xFF) / 255.0f)
            floatArray[i * 3 + 2] = ((pixel and 0xFF) / 255.0f)
        }
        return floatArray
    }

    fun loadLabels(context: Context, fileName: String = "ImageNetLabels.txt"): List<String> {
        return context.assets.open(fileName).bufferedReader().useLines { it.toList() }
    }
} 