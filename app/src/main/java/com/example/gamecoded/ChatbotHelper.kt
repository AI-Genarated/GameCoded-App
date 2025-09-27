package com.example.gamecoded

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.util.Log
import java.nio.LongBuffer
import java.util.Collections

class ChatbotHelper(context: Context) {
    private var env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private lateinit var session: OrtSession

    init {
        val modelPath = "mobile_models/model.onnx"
        try {
            val modelBytes = context.assets.open(modelPath).readBytes()
            session = env.createSession(modelBytes)
            Log.d("ChatbotHelper", "ONNX model loaded successfully with inputs: ${session.inputNames}")
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Failed to load ONNX model: $modelPath", e)
            throw RuntimeException("Failed to initialize ChatbotHelper: Could not load model", e)
        }
    }

    fun getReply(userInput: String): String {
        if (!::session.isInitialized) {
            return "Error: ONNX session not initialized."
        }

        // Placeholder tokenization (replace with real tokenizer)
        val tokenizedInput = listOf(101L, 7592L, 102L) // [CLS] hello [SEP]
        val inputShape = longArrayOf(1, tokenizedInput.size.toLong())
        val inputBuffer = LongBuffer.wrap(tokenizedInput.toLongArray())

        var inputTensor: OnnxTensor? = null

        return try {
            // 🔍 Print model input info
            for (inputName in session.inputNames) {
                Log.d("ChatbotHelper", "Model expects input: $inputName")
            }

            // Create tensor for first input (assumes int64, change if model expects int32)
            val firstInputName = session.inputNames.first()
            inputTensor = OnnxTensor.createTensor(env, inputBuffer, inputShape)

            val inputs = Collections.singletonMap(firstInputName, inputTensor)

            // Run inference
            session.run(inputs).use { results ->
                // 🔍 Log all outputs
                for ((name, value) in results) {
                    Log.d(
                        "ChatbotHelper",
                        "Output -> Name: $name, Type: ${value.type}, Class: ${value.value?.javaClass}"
                    )
                }

                // Try to extract first output
                val firstResult = results.first()
                val outputValue = firstResult?.value

                return when (outputValue) {

                    else -> {
                        "AI: Received output of type ${outputValue?.javaClass}, can’t decode yet."
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Error during ONNX inference", e)
            "AI: Sorry, I encountered an error trying to respond."
        } finally {
            inputTensor?.close()
        }
    }
}
