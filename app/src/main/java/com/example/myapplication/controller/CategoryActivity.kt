package com.example.myapplication.controller

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseItem
import android.content.Intent

class CategoryActivity : AppCompatActivity() {

    private lateinit var database: DatabaseItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_category)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = DatabaseItem(this)

        val btnDongVat = findViewById<Button>(R.id.btnDongVat)
        val btnQuanAo = findViewById<Button>(R.id.btnQuanAo)
        val btnDoVat = findViewById<Button>(R.id.btnDoVat)
        val btnVehicle = findViewById<Button>(R.id.btnVehicle)

        btnDongVat.setOnClickListener {
            navigateToQuestionActivity("dong_vat")
        }

        btnQuanAo.setOnClickListener {
            navigateToQuestionActivity("quan_ao")
        }

        btnDoVat.setOnClickListener {
            navigateToQuestionActivity("do_vat")
        }

        btnVehicle.setOnClickListener {
            navigateToQuestionActivity("vehicle")
        }
    }

    private fun navigateToQuestionActivity(category: String) {
        // Gửi thông tin về category đã chọn sang ActivityQuestion2
        val intent = Intent(this, ActivityQuestion2::class.java)
        intent.putExtra("CATEGORY", category)
        startActivity(intent)
    }
}