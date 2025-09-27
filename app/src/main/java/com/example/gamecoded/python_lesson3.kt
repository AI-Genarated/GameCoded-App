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

class PythonLesson3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_python_lesson3)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val conds = listOf(
            findViewById<TextView>(R.id.cond1),
            findViewById<TextView>(R.id.cond2),
            findViewById<TextView>(R.id.cond3)
        )
        val dropGate = findViewById<TextView>(R.id.dropGate)

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("cond", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }
        conds.forEach { it.setOnLongClickListener(dragStarter) }

        dropGate.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val text = event.clipData.getItemAt(0).text.toString()
                    dropGate.text = "Dropped: $text\nGate opened!"
                }
            }
            true
        }

        val rg = findViewById<RadioGroup>(R.id.rgIf)
        val btn = findViewById<Button>(R.id.btnIfSubmit)
        val tv = findViewById<TextView>(R.id.tvIfResult)

        btn.setOnClickListener {
            val id = rg.checkedRadioButtonId
            if (id == -1) {
                tv.text = "Choose an answer."
                return@setOnClickListener
            }
            val sel = findViewById<RadioButton>(id).text.toString()
            tv.text = if (sel == "Big") "✅ Correct!" else "❌ Try again."
        }
    }
}
