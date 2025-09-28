package com.example.gamecoded

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class ChatBot : AppCompatActivity() {
    private lateinit var bot: ChatbotHelper
    private lateinit var inputBox: EditText
    private lateinit var sendButton: Button
    private lateinit var chatView: TextView
    
    // Coroutine scope for background tasks
    private val activityScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        initializeViews()
        initializeChatbot()
        setupClickListeners()
        
        // Welcome message
        chatView.append("AI: Hello! I'm your AI assistant. How can I help you today?\n\n")
    }

    private fun initializeViews() {
        inputBox = findViewById(R.id.prompt_input)
        sendButton = findViewById(R.id.send_button)
        chatView = findViewById(R.id.chat_view)
        
        // Make chat view scrollable
        chatView.movementMethod = ScrollingMovementMethod()
        
        // Set initial state
        sendButton.isEnabled = false
        sendButton.text = "Loading..."
    }

    private fun initializeChatbot() {
        // Initialize chatbot in background thread
        activityScope.launch(Dispatchers.IO) {
            try {
                bot = ChatbotHelper(this@ChatBot)
                
                // Switch back to main thread to update UI
                withContext(Dispatchers.Main) {
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                    Toast.makeText(this@ChatBot, "Chatbot ready!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                    chatView.append("System: Warning - AI model failed to load. Using fallback responses.\n\n")
                    Toast.makeText(this@ChatBot, "Model loading failed - using fallback mode", Toast.LENGTH_LONG).show()
                }
                
                // Create a fallback bot
                bot = ChatbotHelper(this@ChatBot)
            }
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }
        
        // Allow sending with Enter key
        inputBox.setOnEditorActionListener { _, _, _ ->
            if (sendButton.isEnabled) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val userInput = inputBox.text.toString().trim()
        
        if (userInput.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable send button to prevent multiple rapid sends
        sendButton.isEnabled = false
        sendButton.text = "Sending..."
        
        // Display user message immediately
        chatView.append("You: $userInput\n")
        inputBox.text.clear()
        
        // Scroll to bottom
        scrollToBottom()

        // Get AI response in background
        activityScope.launch(Dispatchers.IO) {
            try {
                val botReply = bot.getReply(userInput)
                
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    chatView.append("$botReply\n\n")
                    scrollToBottom()
                    
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    chatView.append("AI: Sorry, I encountered an error. Please try again.\n\n")
                    scrollToBottom()
                    
                    sendButton.isEnabled = true
                    sendButton.text = "Send"
                    
                    Toast.makeText(this@ChatBot, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun scrollToBottom() {
        chatView.post {
            val scrollAmount = chatView.layout?.let { layout ->
                layout.getLineTop(chatView.lineCount) - chatView.height
            } ?: 0
            
            if (scrollAmount > 0) {
                chatView.scrollTo(0, scrollAmount)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up resources
        if (::bot.isInitialized) {
            activityScope.launch(Dispatchers.IO) {
                bot.cleanup()
            }
        }
        
        // Cancel all coroutines
        activityScope.cancel()
    }

    override fun onBackPressed() {
        // Optional: Add confirmation dialog
        super.onBackPressed()
    }
}
