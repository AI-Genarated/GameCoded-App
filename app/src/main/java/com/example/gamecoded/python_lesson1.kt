package com.example.gamecoded

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PythonLesson1: AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_python_lesson1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val block1: TextView = findViewById(R.id.block1)
        val block2: TextView = findViewById(R.id.block2)
        val block3: TextView = findViewById(R.id.block3)
        val dropTarget: TextView = findViewById(R.id.tvDropTarget)

        val quizOptions: RadioGroup = findViewById(R.id.quizOptions)
        val btnSubmit: Button = findViewById(R.id.btnSubmitQuiz)
        val tvQuizResult: TextView = findViewById(R.id.tvQuizResult)

        // Enable drag on blocks
        val dragListener = View.OnLongClickListener { v ->
            val data = ClipData.newPlainText("block", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(data, shadow, v, 0)
            true
        }

        block1.setOnLongClickListener(dragListener)
        block2.setOnLongClickListener(dragListener)
        block3.setOnLongClickListener(dragListener)

        // Handle drop
        dropTarget.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedText = event.clipData.getItemAt(0).text
                    dropTarget.append(" $draggedText")
                }
            }
            true
        }

        // Quiz logic
        btnSubmit.setOnClickListener {
            val selectedId = quizOptions.checkedRadioButtonId
            if (selectedId == -1) {
                tvQuizResult.text = "Please select an answer!"
            } else {
                val selectedOption: RadioButton = findViewById(selectedId)
                tvQuizResult.text = if (selectedOption.text == "print()") {
                    "✅ Correct! Python uses print() to show text."
                } else {
                    "❌ Oops! Try again."
                }
            }
        }
    }
}