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

class HtmlLesson1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_html_lesson1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_html_lesson1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Drag-and-drop setup
        val tagH1 = findViewById<TextView>(R.id.tagH1)
        val tagP = findViewById<TextView>(R.id.tagP)
        val dropHeading = findViewById<TextView>(R.id.dropHeading)
        val dropParagraph = findViewById<TextView>(R.id.dropParagraph)

        val dragListener = View.OnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val draggedData = event.clipData.getItemAt(0).text.toString()
                    val target = v as TextView

                    if ((draggedData == "<h1>" && target.id == R.id.dropHeading) ||
                        (draggedData == "<p>" && target.id == R.id.dropParagraph)) {
                        target.text = "${target.text} ✔"
                        Toast.makeText(this, "Correct Match!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Try Again!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            true
        }

        // Set long click listeners for dragging
        val dragStartListener = View.OnLongClickListener { v ->
            val data = ClipData.newPlainText("tag", (v as TextView).text)
            v.startDragAndDrop(data, View.DragShadowBuilder(v), null, 0)
            true
        }

        tagH1.setOnLongClickListener(dragStartListener)
        tagP.setOnLongClickListener(dragStartListener)

        dropHeading.setOnDragListener(dragListener)
        dropParagraph.setOnDragListener(dragListener)

        // Quiz handling
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroup)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show()
            } else {
                val selected = findViewById<RadioButton>(selectedId)
                if (selected.id == R.id.option2) {
                    Toast.makeText(this, "Correct! HTML is a markup language.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Oops! Try again.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}