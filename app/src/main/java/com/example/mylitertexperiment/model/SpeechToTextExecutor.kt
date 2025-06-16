package com.example.mylitertexperiment.model

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.TensorBuffer

class SpeechToTextExecutor(context: Context) : BaseModelExecutor<ByteArray, String>() {
    override val outputFlow: SharedFlow<String> = MutableSharedFlow()
    private val _outputFlow = outputFlow as MutableSharedFlow<String>

    private val compiledModel: CompiledModel
    private val inputBuffers: List<TensorBuffer>
    private val outputBuffers: List<TensorBuffer>

    init {
        compiledModel = CompiledModel.create(
            context.assets,
            "conformer-stt.tflite",
            CompiledModel.Options(Accelerator.CPU) // or Accelerator.GPU
        )
        inputBuffers = compiledModel.createInputBuffers()
        outputBuffers = compiledModel.createOutputBuffers()
    }

    override suspend fun runAsync(input: ByteArray) {
        withContext(Dispatchers.Default) {
            // Preprocess ByteArray (PCM audio) to float array
            val floatArray = pcmToFloatArray(input)
            inputBuffers[0].writeFloat(floatArray)

            // Run inference
            compiledModel.run(inputBuffers, outputBuffers)

            // Postprocess: decode output float array to string (model dependent)
            val output = outputBuffers[0].readFloat()
            val speechText = decodeSpeechOutput(output)
            _outputFlow.emit(speechText)
        }
    }

    private fun pcmToFloatArray(audio: ByteArray): FloatArray {
        // Assuming 16-bit PCM, little endian
        val floatArray = FloatArray(audio.size / 2)
        for (i in floatArray.indices) {
            val low = audio[i * 2].toInt() and 0xFF
            val high = audio[i * 2 + 1].toInt()
            val sample = (high shl 8) or low
            floatArray[i] = sample / 32768.0f
        }
        return floatArray
    }

    private fun decodeSpeechOutput(output: FloatArray): String {
        // TODO: Implement decoding logic based on your speech model's output
        // This is a placeholder that just returns a dummy string
        return "[Speech output]"
    }
} 