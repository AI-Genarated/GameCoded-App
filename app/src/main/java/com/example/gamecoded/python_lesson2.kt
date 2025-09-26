package com.example.gamecoded

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.ClipData
import android.view.DragEvent
import android.view.View
import android.widget.*

class python_lesson2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val boxString = findViewById<LinearLayout>(R.id.boxString)
        val boxInt = findViewById<LinearLayout>(R.id.boxInt)
        val boxFloat = findViewById<LinearLayout>(R.id.boxFloat)
        val boxBool = findViewById<LinearLayout>(R.id.boxBool)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson2)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_python_lesson2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("text", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }

        val dropListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val text = event.clipData.getItemAt(0).text.toString()
                    val target = v.id
                    // simple validation feedback: append text to target box
                    val tv = TextView(this)
                    tv.text = text
                    tv.setPadding(6,6,6,6)
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