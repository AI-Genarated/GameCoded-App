package com.example.gamecoded

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context

class ChatbotHelper(context: Context) {
    private var env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private lateinit var session: OrtSession

    init {
        val modelBytes = context.assets.open("mobile_models/model.onnx")
        session = env.createSession(modelBytes)
    }

    fun getReply(userInput: String): String {
        return "Pretend AI: You said '$userInput'"
    }
}