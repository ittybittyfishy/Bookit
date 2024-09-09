package com.example.booknook.fragments

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.content.Intent
import com.example.booknook.R
import com.example.booknook.R.*
import com.example.booknook.R.id.*

class ReviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // XML 파일 연결 (activity_write_review_no_template.xml 사용)
        setContentView(layout.activity_write_review_no_template)
    }
}
