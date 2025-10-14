
package com.example.gamecoded

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var modelRunner: OnxxModelRunner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        modelRunner = OnxxModelRunner(assets)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                ChatScreen(modelRunner)
            }
        }
    }
}

@Composable
fun ChatScreen(modelRunner: OnxxModelRunner) {
    val (inputText, setInputText) = remember { mutableStateOf("") }
    val (responseText, setResponseText) = remember { mutableStateOf("Conversation will appear here.") }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = inputText,
            onValueChange = setInputText,
            label = { Text("Enter your message") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    val response = modelRunner.runInference(inputText)
                    setResponseText("You: $inputText\nBot: $response")
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Send")
        }
        Text(
            text = responseText,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
