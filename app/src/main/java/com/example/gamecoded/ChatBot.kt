package com.example.gamecoded

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ChatBot : AppCompatActivity() {

    private lateinit var chatScrollView: ScrollView
    private lateinit var chatBubblesContainer: LinearLayout
    private lateinit var chatView: TextView
    private lateinit var promptInput: EditText
    private lateinit var sendButton: Button

    private lateinit var chatBotHelper: ChatBotHelper
    private var isModelInitialized = false
    private var conversationHistory = mutableListOf<Pair<String, String>>() // Pair of (user, bot)

    companion object {
        private const val TAG = "ChatBot"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_bot)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_bot_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initializeViews()
        setupChatBotHelper()
        setupClickListeners()
        displayWelcomeMessage()
    }

    private fun initializeViews() {
        chatScrollView = findViewById(R.id.chat_scroll_view)
        chatBubblesContainer = findViewById(R.id.chat_bubbles_container)
        chatView = findViewById(R.id.chat_view)
        promptInput = findViewById(R.id.prompt_input)
        sendButton = findViewById(R.id.send_button)

        // Hide the default chat view initially since we'll create chat bubbles dynamically
        chatView.visibility = View.GONE
    }

    private fun setupChatBotHelper() {
        chatBotHelper = ChatBotHelper(this)

        // Initialize the model asynchronously
        lifecycleScope.launch {
            showLoadingMessage()
            isModelInitialized = chatBotHelper.initializeModel()

            if (isModelInitialized) {
                hideLoadingMessage()
                Log.d(TAG, "ChatBot model initialized successfully")
            } else {
                showErrorMessage("Failed to initialize chatbot. Please restart the app.")
                Log.e(TAG, "Failed to initialize ChatBot model")
            }
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val message = promptInput.text.toString().trim()
            if (message.isNotEmpty() && isModelInitialized) {
                sendMessage(message)
            } else if (!isModelInitialized) {
                showTemporaryMessage("Please wait for the model to finish loading...")
            }
        }

        // Allow sending with Enter key
        promptInput.setOnEditorActionListener { _, _, _ ->
            sendButton.performClick()
            true
        }
    }

    private fun displayWelcomeMessage() {
        val welcomeMessage = """
            🙏 Dumelang! Welcome to Tiro Ya Modimo
            
            I'm your AI assistant ready to help you with questions and conversations. 
            
            Please wait while I initialize...
        """.trimIndent()

        addBotMessage(welcomeMessage)
    }

    private fun sendMessage(userMessage: String) {
        // Clear input field
        promptInput.text.clear()

        // Add user message to chat
        addUserMessage(userMessage)

        // Disable send button while processing
        sendButton.isEnabled = false
        sendButton.text = "..."

        // Generate bot response
        lifecycleScope.launch {
            try {
                val botResponse = chatBotHelper.generateResponse(userMessage)
                addBotMessage(botResponse)

                // Store conversation
                conversationHistory.add(Pair(userMessage, botResponse))

            } catch (e: Exception) {
                Log.e(TAG, "Error generating response: ${e.message}", e)
                addBotMessage("I'm sorry, I encountered an error. Please try again.")
            } finally {
                // Re-enable send button
                sendButton.isEnabled = true
                sendButton.text = "Send"
            }
        }
    }

    private fun addUserMessage(message: String) {
        val userBubble = createChatBubble(message, isUser = true)
        chatBubblesContainer.addView(userBubble)
        scrollToBottom()
    }

    private fun addBotMessage(message: String) {
        val botBubble = createChatBubble(message, isUser = false)
        chatBubblesContainer.addView(botBubble)
        scrollToBottom()
    }

    private fun createChatBubble(message: String, isUser: Boolean): TextView {
        val bubble = TextView(this).apply {
            text = message
            setPadding(32, 24, 32, 24)
            textSize = 16f
            setLineSpacing(4f, 1f)

            if (isUser) {
                // User message styling (right-aligned, blue background)
                background = ContextCompat.getDrawable(this@ChatBot, R.drawable.user_chat_bubble)
                setTextColor(ContextCompat.getColor(this@ChatBot, android.R.color.white))
                gravity = android.view.Gravity.END
            } else {
                // Bot message styling (left-aligned, white background)
                background = ContextCompat.getDrawable(this@ChatBot, R.drawable.bot_chat_bubble)
                setTextColor(ContextCompat.getColor(this@ChatBot, android.R.color.black))
                gravity = android.view.Gravity.START
            }
        }

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 8, 16, 8)
            if (isUser) {
                gravity = android.view.Gravity.END
                marginStart = 64 // More margin on the left for user messages
            } else {
                gravity = android.view.Gravity.START
                marginEnd = 64 // More margin on the right for bot messages
            }
        }

        bubble.layoutParams = layoutParams
        return bubble
    }

    private fun scrollToBottom() {
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun showLoadingMessage() {
        val loadingMessage = "🔄 Loading AI model, please wait..."
        addBotMessage(loadingMessage)
    }

    private fun hideLoadingMessage() {
        // Remove the last message if it's the loading message
        val lastChild = chatBubblesContainer.getChildAt(chatBubblesContainer.childCount - 1) as? TextView
        if (lastChild?.text?.contains("Loading AI model") == true) {
            chatBubblesContainer.removeView(lastChild)
        }

        val readyMessage = "✅ I'm ready to chat! How can I help you today?"
        addBotMessage(readyMessage)
    }

    private fun showErrorMessage(errorMessage: String) {
        addBotMessage("❌ $errorMessage")
    }

    private fun showTemporaryMessage(message: String) {
        // You could implement a Toast or Snackbar here
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        chatBotHelper.cleanup()
    }

    override fun onPause() {
        super.onPause()
        // Save conversation state if needed
        saveConversationState()
    }

    override fun onResume() {
        super.onResume()
        // Restore conversation state if needed
        restoreConversationState()
    }

    private fun saveConversationState() {
        // Implement conversation state saving if needed
        // For example, save to SharedPreferences or local database
    }

    private fun restoreConversationState() {
        // Implement conversation state restoration if needed
    }

    /**
     * Clear chat history
     */
    private fun clearChat() {
        chatBubblesContainer.removeAllViews()
        conversationHistory.clear()
        displayWelcomeMessage()
    }

    /**
     * Get conversation history as formatted string
     */
    private fun getConversationContext(): String {
        return conversationHistory.takeLast(5).joinToString("\n\n") { (user, bot) ->
            "User: $user\nAssistant: $bot"
        }
    }
}