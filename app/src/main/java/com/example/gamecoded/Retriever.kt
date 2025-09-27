
package com.example.gamecoded

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.InputStreamReader
import kotlin.math.sqrt

data class StoreFile(val texts: List<String>, val vectors: List<List<Double>>, val metadata: List<Map<String,Any>>)

class Retriever(private val context: Context) {
    private val gson = Gson()
    private val env = OrtEnvironment.getEnvironment()
    private var session: OrtSession
    private val tokenizer: WordPieceTokenizer
    private val store: StoreFile
    private val seqLen = 128

    init {
        // load store from assets
        val json = context.assets.open("converted_store_with_vectors.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<StoreFile>() {}.type
        store = gson.fromJson(json, type)
        // load ONNX
        val modelBytes = context.assets.open("model.onnx").readBytes()
        session = env.createSession(modelBytes)
        // load tokenizer vocab
        tokenizer = WordPieceTokenizer.fromAssets(context, "tokenizer/vocab.txt")
    }

    private fun toFloatArray(doubles: List<Double>): FloatArray {
        val f = FloatArray(doubles.size)
        for (i in doubles.indices) f[i] = doubles[i].toFloat()
        return f
    }

    private fun cosine(a: FloatArray, b: FloatArray): Float {
        var dot = 0f; var na = 0f; var nb = 0f
        for (i in a.indices) { dot += a[i] * b[i]; na += a[i]*a[i]; nb += b[i]*b[i] }
        return dot / (sqrt(na) * sqrt(nb) + 1e-12f)
    }

    fun embedQuery(text: String): FloatArray {
        // token ids as long[][] for ONNX Tensor
        val inputIds = arrayOf(tokenizer.encode(text, seqLen))
        val attentionMask = arrayOf(LongArray(seqLen) { if (inputIds[0][it] != 0L) 1L else 0L })
        val inputs = mutableMapOf<String, OnnxTensor>()
        // create tensors
        inputs[session.inputNames.iterator().next()] = OnnxTensor.createTensor(env, inputIds)
        // if model expects more inputs, try to provide attention_mask and token_type_ids by name
        val names = session.inputNames
        val listNames = names.toList()
        if (listNames.size > 1) {
            inputs[listNames[1]] = OnnxTensor.createTensor(env, attentionMask)
        }
        if (listNames.size > 2) {
            inputs[listNames[2]] = OnnxTensor.createTensor(env, Array(1) { LongArray(seqLen) { 0L } })
        }

        val results = session.run(inputs)
        try {
            val out = results.get(0).value as Array<FloatArray>
            return out[0]
        } finally {
            results.close()
            inputs.values.forEach { it.close() }
        }
    }

    fun search(queryText: String, topK: Int = 3): List<Pair<String, Double>> {
        val q = embedQuery(queryText)
        val scores = store.vectors.mapIndexed { idx, vec ->
            val v = toFloatArray(vec)
            val score = cosine(q, v).toDouble()
            Triple(idx, score, store.texts[idx])
        }.sortedByDescending { it.second }.take(topK)
        return scores.map { Pair(it.third, it.second) }
    }
}
