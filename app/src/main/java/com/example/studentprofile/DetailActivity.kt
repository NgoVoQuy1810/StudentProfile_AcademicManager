package com.example.studentprofile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val studentId = intent.getStringExtra("KEY_STUDENT_ID")
        val timestamp = intent.getLongExtra("KEY_TIMESTAMP", 0L)
    }
}
