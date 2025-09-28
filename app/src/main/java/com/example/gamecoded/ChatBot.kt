package com.example.gamecoded

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*

class ChatBot : AppCompatActivity() {
    private lateinit var bot: ChatbotHelper
    private lateinit var inputBox: EditText
    private lateinit var sendButton: Button
    private lateinit var chatView: TextView

    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_bot)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_bot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeChatbot()
        setupClickListeners()

        chatView.append("AI: Hello! I'm your AI assistant. How can I help you today?\n\n")
    }

    private fun initializeViews() {
        inputBox = findViewById(R.id.prompt_input)
        sendButton = findViewById(R.id.send_button)
        chatView = findViewById(R.id.chat_view)
        chatView.movementMethod = ScrollingMovementMethod()

        setUiState(isThinking = true)
        sendButton.text = "Loading..."
    }

    private fun initializeChatbot() {
        activityScope.launch(Dispatchers.IO) {
            var isSuccess = false
            try {
                bot = ChatbotHelper(this@ChatBot)
                isSuccess = true
            } catch (e: Exception) {
                bot = ChatbotHelper(this@ChatBot)
            } finally {
                withContext(Dispatchers.Main) {
                    setUiState(isThinking = false)
                    if (isSuccess) {
                        Toast.makeText(this@ChatBot, "Chatbot ready!", Toast.LENGTH_SHORT).show()
                    } else {
                        chatView.append("System: AI is in fallback mode. Responses will be basic.\n\n")
                        Toast.makeText(this@ChatBot, "Model failed to load. Using basic functionality.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }

        inputBox.setOnEditorActionListener { _, _, _ ->
            if (sendButton.isEnabled) {
                sendMessage()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun sendMessage() {
        val userInput = inputBox.text.toString().trim()
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        setUiState(isThinking = true)

        chatView.append("You: $userInput\n")
        inputBox.text.clear()

        // ADDED: Immediate feedback to the user on the Main thread
        // This is replaced by the actual reply once the model returns.
        chatView.append("AI: Thinking...\n")
        scrollToBottom()

        // Launch a new coroutine for the model inference
        activityScope.launch(Dispatchers.IO) {
            val botReply = bot.getReply(userInput) // Long running task

            withContext(Dispatchers.Main) {
                // The 'Thinking...' line is now replaced/overwritten by the actual, verbose reply.
                // A better approach for a real app would be to edit the TextView line.
                // For now, we simply append the detailed response from the helper:
                chatView.append("\n$botReply\n\n")

                scrollToBottom()
                setUiState(isThinking = false)
            }
        }
    }

    private fun setUiState(isThinking: Boolean) {
        sendButton.isEnabled = !isThinking
        sendButton.text = if (isThinking) "Thinking..." else "Send"
    }

    private fun scrollToBottom() {
        chatView.post {
            val layout = chatView.layout
            if (layout != null) {
                val scrollAmount = layout.getLineTop(chatView.lineCount) - chatView.height
                if (scrollAmount > 0) {
                    chatView.scrollTo(0, scrollAmount)
                } else {
                    chatView.scrollTo(0, 0)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::bot.isInitialized) {
            bot.cleanup()
        }
        activityScope.cancel()
    }
}