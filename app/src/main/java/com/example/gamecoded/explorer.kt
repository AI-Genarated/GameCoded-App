package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Explorer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explorer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.explorer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pythonLesson = findViewById<ImageButton>(R.id.python_lesson)
        val htmlLesson = findViewById<ImageButton>(R.id.html_lesson)
        val cssLesson = findViewById<ImageButton>(R.id.css_lesson)
        val javascriptLesson = findViewById<ImageButton>(R.id.javascript_lesson)

        val home = findViewById<ImageButton>(R.id.home_button)
        val lessons = findViewById<ImageButton>(R.id.lessons_button)
        val ai = findViewById<ImageButton>(R.id.ai_button)
        val profile = findViewById<ImageButton>(R.id.profile_button)

        ai.setOnClickListener {
            startActivity(Intent(this, ChatBot::class.java))
        }
        pythonLesson.setOnClickListener {
            startActivity(Intent(this, PythonLessons::class.java))
        }
        htmlLesson.setOnClickListener {
            startActivity(Intent(this, HtmlLessons::class.java))
        }
        cssLesson.setOnClickListener {
            startActivity(Intent(this, CssLessons::class.java))
        }
        javascriptLesson.setOnClickListener {
            startActivity(Intent(this, JavascriptLessons::class.java))
        }

        home.setOnClickListener {
            startActivity(Intent(this, Explorer::class.java))
        }
        lessons.setOnClickListener {
            startActivity(Intent(this, PythonLessons::class.java))
        }
        profile.setOnClickListener {
            startActivity(Intent(this, profile::class.java))
        }
    }
}