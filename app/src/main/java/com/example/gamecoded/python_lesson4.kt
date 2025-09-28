package com.example.gamecoded

import android.content.ClipData
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.DragEvent
import android.view.View
import android.widget.*

class PythonLesson4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lesson4)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_python_lesson4)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val block1 = findViewById<TextView>(R.id.loopBlock1)
        val block2 = findViewById<TextView>(R.id.loopBlock2)
        val loopDrop = findViewById<LinearLayout>(R.id.loopDrop)
        val loopResult = findViewById<TextView>(R.id.loopResult)

        val dragStarter = View.OnLongClickListener { v ->
            val clip = ClipData.newPlainText("code", (v as TextView).text)
            val shadow = View.DragShadowBuilder(v)
            v.startDragAndDrop(clip, shadow, v, 0)
            true
        }
        block1.setOnLongClickListener(dragStarter)
        block2.setOnLongClickListener(dragStarter)

        var hasFor = false
        var hasPrint = false

        loopDrop.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    val text = event.clipData.getItemAt(0).text.toString()
                    if (text.startsWith("for")) hasFor = true
                    if (text.startsWith("print")) hasPrint = true
                    if (hasFor && hasPrint) {
                        // simulate running the loop 3 times
                        val sb = StringBuilder()
                        repeat(3) { sb.append("★ ") }
                        loopResult.text = sb.toString()
                    } else {
                        loopResult.text = "Dropped: $text"
                    }
                }
            }
            true
        }

        // Quiz
        val rg = findViewById<RadioGroup>(R.id.rgLoop)
        val btn = findViewById<Button>(R.id.btnLoopSubmit)
        val tv = findViewById<TextView>(R.id.tvLoopResult)
        btn.setOnClickListener {
            val id = rg.checkedRadioButtonId
            if (id == -1) {
                tv.text = "Pick an answer."
                return@setOnClickListener
            }
            val sel = findViewById<RadioButton>(id).text.toString()
            tv.text = if (sel == "4") "✅ Correct — range(4) runs 4 times!" else "❌ Try again."
        }
    }
}