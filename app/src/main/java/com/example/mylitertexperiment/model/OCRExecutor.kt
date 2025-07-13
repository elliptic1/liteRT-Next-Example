package com.example.mylitertexperiment.model

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.TensorBuffer

class OCRExecutor(context: Context) : BaseModelExecutor<Bitmap, String>() {
    override val outputFlow: SharedFlow<String> = MutableSharedFlow()
    private val _outputFlow = outputFlow as MutableSharedFlow<String>

    private val compiledModel: CompiledModel = CompiledModel.create(
        context.assets,
        "keras-ocr.tflite",
        CompiledModel.Options(Accelerator.CPU) // or Accelerator.GPU
    )
    private val inputBuffers: List<TensorBuffer> = compiledModel.createInputBuffers()
    private val outputBuffers: List<TensorBuffer> = compiledModel.createOutputBuffers()
    private val inputSize = 320 // Example size, adjust to your model

    override suspend fun runAsync(input: Bitmap) {
        withContext(Dispatchers.Default) {
            // Preprocess Bitmap: resize to inputSize x inputSize, normalize to [0,1], NHWC float32
            val resized = Bitmap.createScaledBitmap(input, inputSize, inputSize, true)
            val floatArray = bitmapToNormalizedFloatArray(resized)
            inputBuffers[0].writeFloat(floatArray)

            // Run inference
            try {
                compiledModel.run(inputBuffers, outputBuffers)
            } catch (e: Exception) {
                android.util.Log.e("OCRExecutor", "Exception during model run: ${e.message}")
                _errorFlow.emit("Run error: ${e.message}")
                return@withContext
            }

            // Postprocess: decode output float array to string (model dependent)
            val output = try {
                outputBuffers[0].readFloat()
            } catch (e: Exception) {
                android.util.Log.e("OCRExecutor", "Failed to read output: ${e.message}")
                _errorFlow.emit("Output read error: ${e.message}")
                return@withContext
            }
            val ocrText = decodeOcrOutput(output)
            _outputFlow.emit(ocrText)
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

    private fun decodeOcrOutput(output: FloatArray): String {
        // TODO: Implement decoding logic based on your OCR model's output
        // This is a placeholder that just returns a dummy string
        return "[OCR output]"
    }
} 