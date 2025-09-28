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

class HtmlLesson4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_html_lesson4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_html_lesson4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dragLink = findViewById<TextView>(R.id.drag_a)
        val dragImg = findViewById<TextView>(R.id.drag_img)

        val dropLink = findViewById<LinearLayout>(R.id.drop_link)
        val dropImage = findViewById<LinearLayout>(R.id.drop_image)

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("text", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }
        listOf(dragLink, dragImg).forEach { it.setOnLongClickListener(dragStarter) }

        val dropListener = View.OnDragListener { v, event ->
            if (event.action == DragEvent.ACTION_DROP) {
                val tv = TextView(this)
                tv.text = event.clipData.getItemAt(0).text
                (v as LinearLayout).addView(tv)
            }
            true
        }
        listOf(dropLink, dropImage).forEach { it.setOnDragListener(dropListener) }

        val rg = findViewById<RadioGroup>(R.id.quiz_group)
        val btn = findViewById<Button>(R.id.quiz_submit)
        val tvResult = findViewById<TextView>(R.id.quiz_result)

        btn.setOnClickListener {
            val id = rg.checkedRadioButtonId
            if (id == -1) {
                tvResult.text = "Please select an answer!"
            } else {
                val selected = findViewById<RadioButton>(id).text.toString()
                tvResult.text = if (selected == "<a>") "✅ Correct!" else "❌ Try again."
            }
        }
    }
}