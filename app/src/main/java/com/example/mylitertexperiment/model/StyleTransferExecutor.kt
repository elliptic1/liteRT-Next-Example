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

class StyleTransferExecutor(context: Context) : BaseModelExecutor<Bitmap, Bitmap>() {
    override val outputFlow: SharedFlow<Bitmap> = MutableSharedFlow()
    private val _outputFlow = outputFlow as MutableSharedFlow<Bitmap>

    private val compiledModel: CompiledModel
    private val inputBuffers: List<TensorBuffer>
    private val outputBuffers: List<TensorBuffer>
    private val inputSize = 256 // Example size, adjust to your model

    init {
        compiledModel = CompiledModel.create(
            context.assets,
            "style_transfer.tflite",
            CompiledModel.Options(Accelerator.CPU) // or Accelerator.GPU
        )
        inputBuffers = compiledModel.createInputBuffers()
        outputBuffers = compiledModel.createOutputBuffers()
    }

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
                android.util.Log.e("StyleTransferExecutor", "Exception during model run: ${e.message}")
                _errorFlow.emit("Run error: ${e.message}")
                return@withContext
            }

            // Postprocess: convert output float array to Bitmap
            val output = try {
                outputBuffers[0].readFloat()
            } catch (e: Exception) {
                android.util.Log.e("StyleTransferExecutor", "Failed to read output: ${e.message}")
                _errorFlow.emit("Output read error: ${e.message}")
                return@withContext
            }
            val styledBitmap = floatArrayToBitmap(output, inputSize, inputSize)
            _outputFlow.emit(styledBitmap)
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

    private fun floatArrayToBitmap(floatArray: FloatArray, width: Int, height: Int): Bitmap {
        val pixels = IntArray(width * height)
        val maxIndex = floatArray.size - 1
        for (i in 0 until width * height) {
            val base = i * 3
            val r = if (base <= maxIndex) (floatArray[base] * 255.0f).toInt().coerceIn(0, 255) else 0
            val g = if (base + 1 <= maxIndex) (floatArray[base + 1] * 255.0f).toInt().coerceIn(0, 255) else 0
            val b = if (base + 2 <= maxIndex) (floatArray[base + 2] * 255.0f).toInt().coerceIn(0, 255) else 0
            pixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
} 