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

class PythonLesson5 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson5)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.python_lesson5)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val b1 = findViewById<TextView>(R.id.funcBlock1)
        val b2 = findViewById<TextView>(R.id.funcBlock2)
        val b3 = findViewById<TextView>(R.id.funcBlock3)
        val drop = findViewById<TextView>(R.id.funcDrop)

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("code", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }
        b1.setOnLongClickListener(dragStarter)
        b2.setOnLongClickListener(dragStarter)
        b3.setOnLongClickListener(dragStarter)

        var hasDef = false
        var hasPrint = false
        var hasCall = false

        drop.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val text = event.clipData.getItemAt(0).text.toString()
                    if (text.startsWith("def")) hasDef = true
                    if (text.contains("print")) hasPrint = true
                    if (text.endsWith("()") || text.endsWith(")")) hasCall = true
                    if (hasDef && hasPrint && hasCall) {
                        drop.text = "Function built! Output:\nHi"
                    } else {
                        drop.text = "Dropped: $text"
                    }
                }
            }
            true
        }

        val rg = findViewById<RadioGroup>(R.id.rgFunc)
        val btn = findViewById<Button>(R.id.btnFuncSubmit)
        val tv = findViewById<TextView>(R.id.tvFuncResult)
        btn.setOnClickListener {
            val id = rg.checkedRadioButtonId
            if (id == -1) {
                tv.text = "Choose an answer."
                return@setOnClickListener
            }
            val sel = findViewById<RadioButton>(id).text.toString()
            tv.text = if (sel == "def") "✅ Correct — def creates a function!" else "❌ Not quite."
        }
    }
}