package com.example.myapplication.controller

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.DatabaseItem
import com.example.myapplication.R

class Checkdb : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.check_database) // Tham chiếu tới layout của bạn

        // Lấy TextView từ layout
        val textView = findViewById<TextView>(R.id.checkdb)

        // Khởi tạo DatabaseItem để truy cập cơ sở dữ liệu
        val database = DatabaseItem(this)

        // Lấy tất cả dữ liệu từ bảng
        val images = database.getAllImages()

        // Hiển thị dữ liệu trong TextView
        val stringBuilder = StringBuilder()
        images.forEach { image ->
            stringBuilder.append("ID: ${image.id}\n")
            stringBuilder.append("Image Path: ${image.imagePath}\n")
            stringBuilder.append("Vietnamese Text: ${image.vietnameseText}\n")
            stringBuilder.append("English Text: ${image.englishText}\n")
        }

        textView.text = stringBuilder.toString()
    }
}
