package com.example.myapplication.controller

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.model.DatabaseItem
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseAccount

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
//        val database = DatabaseAccount(this)
//
//        val users = database.getAllUserInfor()
//
//        val stringBuilder = StringBuilder()
//        users.forEach { user ->
//            stringBuilder.append("Username: ${user.username}\n")
//            stringBuilder.append("Name: ${user.name}\n")
//            stringBuilder.append("Birthday: ${user.birthday}\n")
//            stringBuilder.append("Email: ${user.email}\n")
//            stringBuilder.append("Phone: ${user.phone}\n")
//            stringBuilder.append("Address: ${user.address}\n")
//            stringBuilder.append("Avatar URI: ${user.avatarUri ?: "No Avatar"}\n")
//            stringBuilder.append("\n")
//        }
        textView.text = stringBuilder.toString()
    }
}
