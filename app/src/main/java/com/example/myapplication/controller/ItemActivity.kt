package com.example.myapplication.controller

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import java.io.InputStream

class ItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_item)

        // Áp dụng hệ thống thanh trạng thái và điều chỉnh khoảng cách
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton = findViewById<ImageButton>(R.id.backbutton)

        // Lấy các thông tin từ Intent
        val imagePath = intent.getStringExtra("imagePath")
        val vietnameseText = intent.getStringExtra("vietnameseText")
        val englishText = intent.getStringExtra("englishText")

        // Tìm các thành phần giao diện
        val titleItem: TextView = findViewById(R.id.title_item)
        val itemImage: ImageView = findViewById(R.id.itemimage)
        val titleItem1: TextView = findViewById(R.id.title_item1)
        val vietnameseItem: TextView = findViewById(R.id.vietnamese_item)
        val englishItem: TextView = findViewById(R.id.english_item)

        // Cập nhật giao diện với dữ liệu
        titleItem.text = vietnameseText
        titleItem1.text = vietnameseText
        vietnameseItem.text = vietnameseText
        englishItem.text = englishText

        // Tải và hiển thị hình ảnh từ assets
        try {
            val assetManager = assets
            val inputStream: InputStream = assetManager.open(imagePath ?: "")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            itemImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        backButton.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }
}
