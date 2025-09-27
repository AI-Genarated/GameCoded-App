package com.example.gamecoded

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ChatBot : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        val bot = ChatbotHelper(this)

        val inputBox = findViewById<EditText>(R.id.prompt_input)
        val sendButton = findViewById<Button>(R.id.send_button)
        val chatView = findViewById<TextView>(R.id.chat_view)

        sendButton.setOnClickListener {
            val userInput = inputBox.text.toString()
            val botReply = bot.getReply(userInput)

            chatView.append("You: $userInput\n")
            chatView.append("$botReply\n")

            inputBox.text.clear()
        }
    }
}