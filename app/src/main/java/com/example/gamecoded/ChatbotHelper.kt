package com.example.gamecoded

import android.content.Context
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtSession.Result
import java.nio.LongBuffer

class ChatbotHelper(private val context: Context) {

    companion object {
        private const val MODEL_PATH = "mobile_models/model.onnx"
        private const val VOCAB_PATH = "mobile_models/vocab.txt"
        private const val MAX_RESPONSE_LENGTH = 50
        private const val TAG = "ChatbotHelper"
    }

    private var ortEnvironment: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private lateinit var tokenizer: Tokenizer
    private var isInitialized = false

    private lateinit var inputName: String
    private lateinit var outputName: String

    init {
        initialize()
    }

    private fun initialize() {
        println("$TAG: === Chatbot Initialization Started ===")
        try {
            println("$TAG: Initializing tokenizer from assets: $VOCAB_PATH")
            tokenizer = Tokenizer(context, VOCAB_PATH)
            println("$TAG: ✓ Tokenizer initialized successfully. Vocab size: ${tokenizer.getVocabSize()}")

            println("$TAG: Creating ORT Environment...")
            ortEnvironment = OrtEnvironment.getEnvironment()
            println("$TAG: ✓ ORT Environment created.")

            println("$TAG: Loading model from assets: $MODEL_PATH")
            val modelBytes = context.assets.open(MODEL_PATH).readBytes()
            println("$TAG: ✓ Model loaded into memory (${modelBytes.size} bytes).")

            ortSession = ortEnvironment!!.createSession(modelBytes)
            println("$TAG: ✓ ORT Session created.")

            inputName = ortSession!!.inputNames.first()
            outputName = ortSession!!.outputNames.first()
            println("$TAG: ✓ Model introspection complete:")
            println("$TAG:   - Input Name: '$inputName'")
            println("$TAG:   - Output Name: '$outputName'")

            isInitialized = true
            println("$TAG: === Initialization Complete - SUCCESS ===")

        } catch (e: Exception) {
            isInitialized = false
            println("$TAG: ❌ ERROR: Initialization failed: ${e.message}")
            e.printStackTrace()
            println("$TAG: === Initialization Complete - FAILED ===")
        }
    }

    private fun runInference(inputIdsData: LongArray): Result? {

        val ortEnv = ortEnvironment ?: return null
        val session = ortSession ?: return null

        val tensorsToClose = mutableListOf<OnnxTensor>()

        try {
            val sequenceLength = inputIdsData.size.toLong()
            val shape = longArrayOf(1, sequenceLength)

            // FIX: Use LongBuffer.wrap() for input_ids
            val inputIdsTensor = OnnxTensor.createTensor(
                ortEnv,
                LongBuffer.wrap(inputIdsData),
                shape
            )
            tensorsToClose.add(inputIdsTensor)

            // FIX: Use LongBuffer.wrap() for token_type_ids
            val tokenTypeIdsData = LongArray(inputIdsData.size) { 0L }
            val tokenTypeIdsTensor = OnnxTensor.createTensor(
                ortEnv,
                LongBuffer.wrap(tokenTypeIdsData),
                shape
            )
            tensorsToClose.add(tokenTypeIdsTensor)

            // FIX: Use LongBuffer.wrap() for attention_mask
            val attentionMaskData = LongArray(inputIdsData.size) { 1L }
            val attentionMaskTensor = OnnxTensor.createTensor(
                ortEnv,
                LongBuffer.wrap(attentionMaskData),
                shape
            )
            tensorsToClose.add(attentionMaskTensor)

            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "token_type_ids" to tokenTypeIdsTensor,
                "attention_mask" to attentionMaskTensor
            )

            return session.run(inputs, setOf(outputName))

        } catch (e: OrtException) {
            // FIX: Use getErrorCode()
            println("$TAG: ❌ ERROR: Inference failed: Error code - ${e.code} - message: ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            // FIX: Ensure all input tensors are closed
            tensorsToClose.forEach { it.close() }
        }
    }

    private fun generateResponse(userInput: String): String {
        val tokens = tokenizer.encode(userInput)
        val inputIdsData = tokens.toLongArray()

        val result: Result? = runInference(inputIdsData)

        // The model ran successfully but needs a generation loop.
        return result?.use {
            "SUCCESS: Model inference complete. The result is an embedding. Waiting for implementation of the text generation loop."
        } ?: "ERROR: Model inference failed. See logs for OrtException details."
    }

    fun getReply(userInput: String): String {
        println("$TAG: Processing user input: '$userInput'")
        if (!isInitialized) {
            println("$TAG: Model not initialized. Using fallback response.")
            return getFallbackResponse(userInput)
        }

        if (userInput.isBlank()) {
            return "AI: Please say something!"
        }

        return try {
            val statusMessage = generateResponse(userInput)

            // If the model ran successfully, return the instructional message for the user.
            if (statusMessage.startsWith("SUCCESS:")) {
                // The main activity handles the "Thinking..." text, so we return a friendly reply here.
                return "AI: I ran the model successfully! However, the code is currently returning a status message instead of a real reply because the **text generation/decoding logic** is not yet implemented. Please check back later for a full chat update! $statusMessage"
            }

            // Return a general error message if inference failed
            "AI: Inference Error: Could not get a reply. Please check the system logs. ${statusMessage.replace("ERROR:", "")}"

        } catch (e: Exception) {
            println("$TAG: ❌ ERROR: Inference failed: ${e.message}")
            e.printStackTrace()
            getFallbackResponse(userInput)
        }
    }

    private fun getFallbackResponse(input: String): String {
        val lowerInput = input.lowercase().trim()
        val responses = mapOf(
            "hello" to "Hello! How can I help you with coding today?",
            "hi" to "Hi there! Ready to learn some code?",
            "help" to "Of course! What programming concept are you curious about?",
            "bye" to "Goodbye! Happy coding!"
        )

        return responses.entries.find { lowerInput.contains(it.key) }?.value
            ?: "AI: I'm not sure how to answer that. Try asking me about coding! (Fallback mode is active.)"
    }

    fun cleanup() {
        try {
            println("$TAG: Cleaning up resources.")
            ortSession?.close()
            ortEnvironment?.close()
            isInitialized = false
        } catch (e: Exception) {
            println("$TAG: ❌ ERROR during cleanup: ${e.message}")
        }
    }
}

class Tokenizer(context: Context, vocabPath: String) {
    private val vocabulary: Map<String, Int>
    private val reverseVocabulary: Map<Int, String>

    val eosTokenId: Int
    private val unkTokenId: Int

    init {
        val vocabList = context.assets.open(vocabPath).bufferedReader().readLines()
        vocabulary = vocabList.mapIndexed { index, token -> token to index }.toMap()
        reverseVocabulary = vocabulary.entries.associate { (k, v) -> v to k }

        eosTokenId = vocabulary["[SEP]"] ?: throw IllegalArgumentException("Vocabulary must contain an end-of-sequence token like '[SEP]'")
        unkTokenId = vocabulary["<unk>"] ?: throw IllegalArgumentException("Vocabulary must contain an unknown token like '<unk>'")
    }

    fun getVocabSize(): Int = vocabulary.size

    fun encode(text: String): List<Long> {
        val tokens = mutableListOf<Long>()
        tokens.add(vocabulary["[CLS]"]!!.toLong())

        val words = text.lowercase().trim().split(Regex("\\s+"))
        for (word in words) {
            val tokenId = vocabulary[word] ?: unkTokenId
            tokens.add(tokenId.toLong())
        }
        tokens.add(eosTokenId.toLong())
        return tokens
    }

    fun decode(tokens: List<Long>): String {
        val startIndex = tokens.indexOf(vocabulary["[CLS]"]!!.toLong()) + 1
        return tokens.subList(startIndex, tokens.size)
            .mapNotNull { reverseVocabulary[it.toInt()] }
            .joinToString(" ")
            .replace(" ##", "")
    }
}