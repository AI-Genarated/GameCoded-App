package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PythonLessons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_python_lessons)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.python_lessons)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val python_Lesson1 = findViewById<Button>(R.id.lesson1)
        val python_Lesson2 = findViewById<Button>(R.id.lesson2)
        val python_Lesson3 = findViewById<Button>(R.id.lesson3)
        val python_Lesson4 = findViewById<Button>(R.id.lesson4)
        val python_Lesson5 = findViewById<Button>(R.id.lesson5)

        python_Lesson1.setOnClickListener {
            startActivity(Intent(this, PythonLesson1::class.java))
        }
        python_Lesson2.setOnClickListener {
            startActivity(Intent(this, PythonLesson2::class.java))
        }
        python_Lesson3.setOnClickListener {
            startActivity(Intent(this, PythonLesson3::class.java))
        }
        python_Lesson4.setOnClickListener {
            startActivity(Intent(this, PythonLesson4::class.java))
        }
        python_Lesson5.setOnClickListener {
            startActivity(Intent(this, PythonLesson5::class.java))
        }
    }
}