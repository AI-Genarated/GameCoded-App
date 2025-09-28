package com.example.gamecoded

import android.content.ClipData
import android.os.Bundle
import android.view.DragEvent
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PythonLesson2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson2)

        // Fix: findViewById AFTER setContentView
        val boxString = findViewById<LinearLayout>(R.id.boxString)
        val boxInt = findViewById<LinearLayout>(R.id.boxInt)
        val boxFloat = findViewById<LinearLayout>(R.id.boxFloat)
        val boxBool = findViewById<LinearLayout>(R.id.boxBool)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_python_lesson2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val examples = listOf(
            findViewById<TextView>(R.id.varExample1),
            findViewById<TextView>(R.id.varExample2),
            findViewById<TextView>(R.id.varExample3),
            findViewById<TextView>(R.id.varExample4)
        )

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("text", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }
        examples.forEach { it.setOnLongClickListener(dragStarter) }

        val dropListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val text = event.clipData.getItemAt(0).text.toString()
                    val tv = TextView(this)
                    tv.text = text
                    tv.setPadding(6, 6, 6, 6)
                    (v as LinearLayout).addView(tv)
                }
            }
            true
        }

        listOf(boxString, boxInt, boxFloat, boxBool).forEach { it.setOnDragListener(dropListener) }

        // Quiz
        val rg = findViewById<RadioGroup>(R.id.rgVariables)
        val btn = findViewById<Button>(R.id.btnVarSubmit)
        val tvResult = findViewById<TextView>(R.id.tvVarResult)

        btn.setOnClickListener {
            val id = rg.checkedRadioButtonId
            if (id == -1) {
                tvResult.text = "Please choose an answer."
                return@setOnClickListener
            }
            val selected = findViewById<RadioButton>(id).text.toString()
            tvResult.text = if (selected == "float") {
                "✅ Correct — 9.99 is a float!"
            } else {
                "❌ Not quite — try again."
            }
        }
    }
}
