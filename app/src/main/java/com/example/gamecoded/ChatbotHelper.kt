package com.example.gamecoded

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.util.Log
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.util.Collections

class ChatbotHelper(private val context: Context) {
    private var env: OrtEnvironment? = null
    private var session: OrtSession? = null
    private var isInitialized = false
    
    // Simple vocabulary for demonstration - replace with your model's vocab
    private val vocabulary = mapOf(
        "<pad>" to 0L, "<unk>" to 1L, "[CLS]" to 101L, "[SEP]" to 102L,
        "hello" to 7592L, "hi" to 7593L, "how" to 2129L, "are" to 2024L,
        "you" to 2017L, "what" to 2054L, "is" to 2003L, "your" to 2115L,
        "name" to 2171L, "?" to 1029L, "good" to 2204L, "fine" to 2486L,
        "thanks" to 4283L, "bye" to 11036L, "help" to 2393L
    )

    init {
        initializeModel()
    }

    private fun initializeModel() {
        val modelPath = "mobile_models/model.onnx"
        try {
            // Check if model file exists
            val inputStream = context.assets.open(modelPath)
            val modelBytes = inputStream.readBytes()
            inputStream.close()
            
            env = OrtEnvironment.getEnvironment()
            session = env?.createSession(modelBytes)
            
            session?.let { sess ->
                Log.d("ChatbotHelper", "ONNX model loaded successfully")
                Log.d("ChatbotHelper", "Input names: ${sess.inputNames}")
                Log.d("ChatbotHelper", "Output names: ${sess.outputNames}")
                
                // Print input info for debugging (safer approach)
                logModelInputInfo(sess)
                
                isInitialized = true
            }
        } catch (e: IOException) {
            Log.e("ChatbotHelper", "Model file not found: $modelPath", e)
            isInitialized = false
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Failed to load ONNX model: $modelPath", e)
            isInitialized = false
        }
    }

    private fun logModelInputInfo(session: OrtSession) {
        try {
            // Log input names (this always works)
            Log.d("ChatbotHelper", "Input names: ${session.inputNames.joinToString(", ")}")
            Log.d("ChatbotHelper", "Output names: ${session.outputNames.joinToString(", ")}")
            
            // Try to get input info using the correct method signature
            try {
                val inputInfoMap = session.getInputInfo()
                for ((name, nodeInfo) in inputInfoMap) {
                    Log.d("ChatbotHelper", "Input '$name': $nodeInfo")
                }
            } catch (e: Exception) {
                Log.d("ChatbotHelper", "Could not get input info map: ${e.message}")
            }
            
            // Try to get output info using the correct method signature
            try {
                val outputInfoMap = session.getOutputInfo()
                for ((name, nodeInfo) in outputInfoMap) {
                    Log.d("ChatbotHelper", "Output '$name': $nodeInfo")
                }
            } catch (e: Exception) {
                Log.d("ChatbotHelper", "Could not get output info map: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.w("ChatbotHelper", "Could not retrieve model metadata: ${e.message}")
        }
    }

    private fun isResultEmpty(results: OrtSession.Result): Boolean {
        return try {
            if (results.size() == 0) {
                Log.d("ChatbotHelper", "Results container is empty")
                true
            } else {
                var hasValidOutput = false
                for ((name, ortValue) in results) {
                    Log.d("ChatbotHelper", "Checking output '$name': ${ortValue?.javaClass?.simpleName}")
                    
                    val value = ortValue?.value
                    when {
                        value == null -> {
                            Log.d("ChatbotHelper", "Output '$name' has null value")
                        }
                        value is Array<*> && value.isEmpty() -> {
                            Log.d("ChatbotHelper", "Output '$name' is empty array")
                        }
                        value is FloatArray && value.isEmpty() -> {
                            Log.d("ChatbotHelper", "Output '$name' is empty FloatArray")
                        }
                        value is LongArray && value.isEmpty() -> {
                            Log.d("ChatbotHelper", "Output '$name' is empty LongArray")
                        }
                        else -> {
                            Log.d("ChatbotHelper", "Output '$name' has valid data")
                            hasValidOutput = true
                        }
                    }
                }
                !hasValidOutput
            }
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Error checking if results are empty", e)
            true // Assume empty on error
        }
    }

    private fun tokenize(text: String): List<Long> {
        val tokens = mutableListOf<Long>()
        tokens.add(101L) // [CLS] token
        
        // Simple word-level tokenization
        val words = text.lowercase().split(" ")
        for (word in words) {
            val cleanWord = word.replace(Regex("[^a-z?]"), "")
            tokens.add(vocabulary[cleanWord] ?: 1L) // Use <unk> for unknown words
        }
        
        tokens.add(102L) // [SEP] token
        return tokens
    }

    private fun processModelOutput(results: OrtSession.Result): String {
        return try {
            if (isResultEmpty(results)) {
                return "AI: No valid output from model"
            }

            val firstOutput = results.first()
            val outputValue = firstOutput?.value

            Log.d("ChatbotHelper", "Processing output of type: ${outputValue?.javaClass?.simpleName}")

            when (outputValue) {
                is Array<*> -> {
                    Log.d("ChatbotHelper", "Array output with ${outputValue.size} elements")
                    if (outputValue.isNotEmpty()) {
                        when (val firstElement = outputValue[0]) {
                            is FloatArray -> {
                                Log.d("ChatbotHelper", "FloatArray with ${firstElement.size} elements")
                                if (firstElement.isNotEmpty()) {
                                    val maxIndex = firstElement.indices.maxByOrNull { firstElement[it] } ?: 0
                                    val confidence = firstElement[maxIndex]
                                    generateResponseFromLogits(maxIndex, confidence)
                                } else {
                                    "AI: Empty probability array"
                                }
                            }
                            is Array<*> -> {
                                Log.d("ChatbotHelper", "Nested array with ${firstElement.size} elements")
                                "AI: Received nested array output - processing not implemented yet"
                            }
                            else -> {
                                Log.d("ChatbotHelper", "Array element type: ${firstElement?.javaClass?.simpleName}")
                                "AI: Output format: ${firstElement?.javaClass?.simpleName}"
                            }
                        }
                    } else {
                        "AI: Empty array output"
                    }
                }
                is FloatArray -> {
                    Log.d("ChatbotHelper", "Direct FloatArray with ${outputValue.size} elements")
                    if (outputValue.isNotEmpty()) {
                        val maxIndex = outputValue.indices.maxByOrNull { outputValue[it] } ?: 0
                        val confidence = outputValue[maxIndex]
                        generateResponseFromLogits(maxIndex, confidence)
                    } else {
                        "AI: Empty float array"
                    }
                }
                is LongArray -> {
                    Log.d("ChatbotHelper", "LongArray with ${outputValue.size} elements")
                    if (outputValue.isNotEmpty()) {
                        val tokens = outputValue.toList()
                        decodeTokens(tokens)
                    } else {
                        "AI: Empty token array"
                    }
                }
                is IntArray -> {
                    Log.d("ChatbotHelper", "IntArray with ${outputValue.size} elements")
                    if (outputValue.isNotEmpty()) {
                        val tokens = outputValue.map { it.toLong() }
                        decodeTokens(tokens)
                    } else {
                        "AI: Empty int array"
                    }
                }
                else -> {
                    Log.w("ChatbotHelper", "Unhandled output type: ${outputValue?.javaClass}")
                    "AI: Output type ${outputValue?.javaClass?.simpleName} - decoder needed"
                }
            }
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Error processing model output", e)
            "AI: Error processing response: ${e.message}"
        }
    }

    private fun generateResponseFromLogits(maxIndex: Int, confidence: Float): String {
        // Simple response generation based on output class
        val responses = arrayOf(
            "Hello! How can I help you today?",
            "Hi there! Nice to meet you!",
            "I'm doing well, thank you for asking!",
            "That's interesting! Tell me more.",
            "I'm here to help with your questions.",
            "Thanks for chatting with me!"
        )
        
        val responseIndex = maxIndex % responses.size
        return "AI: ${responses[responseIndex]} (confidence: ${String.format("%.2f", confidence)})"
    }

    private fun decodeTokens(tokens: List<Long>): String {
        val reverseVocab = vocabulary.entries.associate { it.value to it.key }
        val words = mutableListOf<String>()
        
        for (token in tokens) {
            if (token == 101L || token == 102L) continue // Skip special tokens
            val word = reverseVocab[token] ?: "<unk>"
            if (word != "<pad>" && word != "<unk>") {
                words.add(word)
            }
        }
        
        return if (words.isNotEmpty()) {
            "AI: ${words.joinToString(" ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
        } else {
            "AI: I understand, but I'm still learning how to respond!"
        }
    }

    fun getReply(userInput: String): String {
        if (!isInitialized) {
            return "AI: Sorry, the model isn't loaded. Please check if the model file exists in assets/mobile_models/model.onnx"
        }

        if (userInput.trim().isEmpty()) {
            return "AI: Please enter a message!"
        }

        val session = this.session ?: return "AI: Session not available"
        
        // Fallback responses for common inputs (while model is being debugged)
        val fallbackResponses = mapOf(
            "hello" to "AI: Hello! How are you doing today?",
            "hi" to "AI: Hi there! What's on your mind?",
            "how are you" to "AI: I'm doing great! Thanks for asking. How about you?",
            "what is your name" to "AI: I'm your friendly AI assistant! What's your name?",
            "help" to "AI: I'm here to help! What do you need assistance with?",
            "bye" to "AI: Goodbye! Have a wonderful day!"
        )
        
        val lowerInput = userInput.lowercase().trim()
        fallbackResponses[lowerInput]?.let { return it }

        // Try ONNX model inference
        return try {
            val tokenizedInput = tokenize(userInput)
            Log.d("ChatbotHelper", "Tokenized input: $tokenizedInput")
            
            val inputShape = longArrayOf(1, tokenizedInput.size.toLong())
            val inputBuffer = LongBuffer.wrap(tokenizedInput.toLongArray())
            
            val firstInputName = session.inputNames.first()
            
            OnnxTensor.createTensor(env, inputBuffer, inputShape).use { inputTensor ->
                val inputs = Collections.singletonMap(firstInputName, inputTensor)
                
                session.run(inputs).use { results ->
                    Log.d("ChatbotHelper", "Model inference completed successfully")
                    if (isResultEmpty(results)) {
                        Log.w("ChatbotHelper", "Model returned empty results")
                        return "AI: The model didn't return any meaningful output. This might be a model configuration issue."
                    }
                    processModelOutput(results)
                }
            }
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Error during ONNX inference", e)
            // Provide a helpful fallback
            "AI: I'm having trouble processing that right now, but I'm here to chat! Try asking me something simple like 'hello' or 'how are you'."
        }
    }

    fun cleanup() {
        try {
            session?.close()
            env?.close()
            session = null
            env = null
            isInitialized = false
            Log.d("ChatbotHelper", "ChatbotHelper cleaned up successfully")
        } catch (e: Exception) {
            Log.e("ChatbotHelper", "Error during cleanup", e)
        }
    }
}
