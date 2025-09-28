package com.example.gamecoded

import android.content.Context
import android.util.Log
import ai.onnxruntime.*
import java.nio.FloatBuffer
import java.nio.LongBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatBotHelper(private val context: Context) {

    private var ortSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null

    // Model parameters (adjust these based on your specific model)
    private val maxSequenceLength = 512
    private val vocabSize = 50257 // Common for GPT-like models
    private val hiddenSize = 768

    // Tokenizer mappings (simplified - in production use a proper tokenizer)
    private val tokenToId = mutableMapOf<String, Long>()
    private val idToToken = mutableMapOf<Long, String>()

    companion object {
        private const val TAG = "ChatBotHelper"
        private const val MODEL_NAME = "model.onnx" // Place your ONNX model in assets folder

        // Special tokens
        const val BOS_TOKEN_ID = 50256L
        const val EOS_TOKEN_ID = 50256L
        const val PAD_TOKEN_ID = 50257L
    }

    init {
        initializeTokenizer()
    }

    /**
     * Initialize the ONNX Runtime session with the model
     */
    suspend fun initializeModel(): Boolean = withContext(Dispatchers.IO) {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()

            // Load model from assets
            val modelBytes = context.assets.open(MODEL_NAME).use { inputStream ->
                inputStream.readBytes()
            }

            val sessionOptions = OrtSession.SessionOptions().apply {
                // Configure session options if needed
                setIntraOpNumThreads(1)
                setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT)
            }

            ortSession = ortEnvironment?.createSession(modelBytes, sessionOptions)

            Log.d(TAG, "Model initialized successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model: ${e.message}", e)
            false
        }
    }

    /**
     * Generate text response based on input prompt
     */
    suspend fun generateResponse(prompt: String, maxTokens: Int = 50): String = withContext(Dispatchers.IO) {
        try {
            val session = ortSession ?: return@withContext "Model not initialized"

            // Tokenize input
            val inputTokens = tokenize(prompt).takeLast(maxSequenceLength - maxTokens)
            val inputIds = LongArray(inputTokens.size) { inputTokens[it] }

            // Create input tensors
            val inputShape = longArrayOf(1, inputIds.size.toLong())
            val inputTensor = OnnxTensor.createTensor(
                ortEnvironment,
                LongBuffer.wrap(inputIds),
                inputShape
            )

            var inputs = mapOf("input_ids" to inputTensor)

            // Generate tokens one by one
            val generatedTokens = mutableListOf<Long>()
            var currentInputIds = inputIds.toMutableList()

            repeat(maxTokens) {
                // Run inference
                val outputs = session.run(inputs)

                // Get logits from output (assuming output name is "logits")
                val logits = outputs.get("logits").get() as OnnxTensor
                val logitsData = logits.floatBuffer

                // Simple sampling - take the token with highest probability
                val nextTokenId = sampleNextToken(logitsData, currentInputIds.size - 1)

                if (nextTokenId == EOS_TOKEN_ID)

                generatedTokens.add(nextTokenId)
                currentInputIds.add(nextTokenId)

                // Update input for next iteration
                val newInputShape = longArrayOf(1, currentInputIds.size.toLong())
                val newInputTensor = OnnxTensor.createTensor(
                    ortEnvironment,
                    LongBuffer.wrap(currentInputIds.toLongArray()),
                    newInputShape
                )
                inputs["input_ids"]?.close()
                inputs = mapOf("input_ids" to newInputTensor)
            }

            // Clean up
            inputs.values.forEach { it.close() }

            // Decode generated tokens to text
            detokenize(generatedTokens)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating response: ${e.message}", e)
            "Sorry, I encountered an error while processing your request."
        }
    }

    /**
     * Simple tokenization (in production, use a proper tokenizer like SentencePiece or BPE)
     */
    private fun tokenize(text: String): List<Long> {
        // This is a very simplified tokenizer
        // In production, you should use the same tokenizer that was used to train the model
        val words = text.lowercase().split("\\s+".toRegex())
        return words.mapNotNull { word ->
            tokenToId[word] ?: tokenToId["<unk>"]
        }
    }

    /**
     * Convert token IDs back to text
     */
    private fun detokenize(tokenIds: List<Long>): String {
        return tokenIds.mapNotNull { id ->
            idToToken[id]
        }.joinToString(" ")
    }

    /**
     * Sample next token from logits (simplified sampling)
     */
    private fun sampleNextToken(logits: FloatBuffer, position: Int): Long {
        // Get logits for the last position
        val vocabStart = position * vocabSize
        val vocabLogits = FloatArray(vocabSize)

        logits.position(vocabStart)
        logits.get(vocabLogits, 0, vocabSize)

        // Apply softmax and sample (simplified - just take argmax)
        var maxIndex = 0
        var maxValue = vocabLogits[0]

        for (i in 1 until vocabLogits.size) {
            if (vocabLogits[i] > maxValue) {
                maxValue = vocabLogits[i]
                maxIndex = i
            }
        }

        return maxIndex.toLong()
    }

    /**
     * Initialize a simple tokenizer vocabulary
     * In production, load this from the tokenizer files
     */
    private fun initializeTokenizer() {
        // Add some basic tokens (this is very simplified)
        val commonWords = listOf(
            "hello", "hi", "how", "are", "you", "what", "is", "the", "a", "an",
            "and", "or", "but", "in", "on", "at", "to", "from", "with", "by",
            "for", "of", "as", "if", "then", "else", "can", "could", "would",
            "should", "will", "shall", "may", "might", "must", "have", "has",
            "had", "do", "does", "did", "be", "am", "is", "are", "was", "were",
            "been", "being", "go", "went", "gone", "come", "came", "see", "saw",
            "get", "got", "give", "gave", "take", "took", "make", "made",
            "think", "thought", "know", "knew", "say", "said", "tell", "told",
            "good", "bad", "great", "nice", "fine", "okay", "yes", "no",
            "please", "thank", "thanks", "sorry", "excuse", "help", "time",
            "day", "night", "morning", "evening", "today", "tomorrow", "yesterday",
            "<unk>", "<pad>", "<bos>", "<eos>"
        )

        commonWords.forEachIndexed { index, word ->
            tokenToId[word] = index.toLong()
            idToToken[index.toLong()] = word
        }

        // Add special token mappings
        tokenToId["<unk>"] = (commonWords.size - 4).toLong()
        tokenToId["<pad>"] = PAD_TOKEN_ID
        tokenToId["<bos>"] = BOS_TOKEN_ID
        tokenToId["<eos>"] = EOS_TOKEN_ID
    }

    /**
     * Log model input and output information for debugging
     */
    private fun logModelInfo() {
        try {
            val session = ortSession ?: return

            Log.d(TAG, "=== Model Information ===")

            // Log input names and shapes
            Log.d(TAG, "Model Inputs:")
            session.inputNames.forEach { inputName ->
                val inputInfo = session.inputInfo[inputName]
                Log.d(TAG, "  - $inputName: ${inputInfo?.info}")
            }

            // Log output names and shapes
            Log.d(TAG, "Model Outputs:")
            session.outputNames.forEach { outputName ->
                val outputInfo = session.outputInfo[outputName]
                Log.d(TAG, "  - $outputName: ${outputInfo?.info}")
            }

            Log.d(TAG, "========================")

        } catch (e: Exception) {
            Log.e(TAG, "Error logging model info: ${e.message}", e)
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            ortSession?.close()
            ortEnvironment?.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up resources: ${e.message}", e)
        }
    }
}