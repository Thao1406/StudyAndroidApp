package com.example.myapplication.controller

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import java.io.InputStream
import java.util.*

class ItemActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var vietnameseItem: TextView
    private lateinit var englishItem: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        // Ánh xạ các View
        val backButton = findViewById<ImageButton>(R.id.backbutton)
        val vnSpeakerButton = findViewById<ImageButton>(R.id.vn_speaker)
        val engSpeakerButton = findViewById<ImageButton>(R.id.eng_speaker)

        vietnameseItem = findViewById(R.id.vietnamese_item)
        englishItem = findViewById(R.id.english_item)

        val titleItem = findViewById<TextView>(R.id.title_item)
        val titleItem1 = findViewById<TextView>(R.id.title_item1)
        val itemImage = findViewById<ImageView>(R.id.itemimage)

        // Lấy dữ liệu từ Intent
        val imagePath = intent.getStringExtra("imagePath")
        val vietnameseText = intent.getStringExtra("vietnameseText")
        val englishText = intent.getStringExtra("englishText")

        // Cập nhật dữ liệu vào giao diện
        titleItem.text = vietnameseText
        titleItem1.text = vietnameseText
        vietnameseItem.text = vietnameseText
        englishItem.text = englishText

        // Hiển thị hình ảnh
        try {
            val assetManager = assets
            val inputStream: InputStream = assetManager.open(imagePath ?: "")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            itemImage.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Khởi tạo Text-to-Speech
        tts = TextToSpeech(this, this)

        // Nút quay lại
        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Sự kiện phát giọng đọc tiếng Việt
        vnSpeakerButton.setOnClickListener {
            val text = vietnameseItem.text.toString()
            if (text.isNotEmpty()) {
                speak(text, Locale("vi", "VN"))
            } else {
                Toast.makeText(this, "Không có nội dung để đọc!", Toast.LENGTH_SHORT).show()
            }
        }

        // Sự kiện phát giọng đọc tiếng Anh
        engSpeakerButton.setOnClickListener {
            val text = englishItem.text.toString()
            if (text.isNotEmpty()) {
                speak(text, Locale.US)
            } else {
                Toast.makeText(this, "Không có nội dung để đọc!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Khởi tạo TTS
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.US // Đặt ngôn ngữ mặc định
        } else {
            Toast.makeText(this, "Khởi tạo Text-to-Speech thất bại!", Toast.LENGTH_SHORT).show()
        }
    }

    // Hàm phát giọng đọc
    private fun speak(text: String, locale: Locale) {
        tts.language = locale
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        // Dọn dẹp tài nguyên TTS
        if (tts != null) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
