package com.example.myapplication.controller

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseItem
import com.example.myapplication.model.ImageRecognition
import android.content.res.AssetManager
import com.airbnb.lottie.LottieAnimationView
import android.speech.tts.TextToSpeech
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.view.View
import java.io.InputStream
import java.util.Locale

class ActivityQuestion2 : AppCompatActivity() {

    private lateinit var database: DatabaseItem
    private lateinit var category: String
    private lateinit var correctImage: ImageRecognition
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var fireworksAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Cho phép hiển thị chế độ "edge-to-edge"
        setContentView(R.layout.activity_question2)
        fireworksAnimation = findViewById(R.id.fireworksAnimation)

        // Áp dụng window insets listener để đảm bảo các padding cho hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("vi", "VN") // Đặt ngôn ngữ phát âm
                val questionText = "Hãy chọn ${correctImage.vietnameseText}"
                playQuestion(questionText)
            } else {
                Toast.makeText(this, "Không thể khởi tạo TextToSpeech!", Toast.LENGTH_SHORT).show()
            }
        }


        // Khởi tạo database
        database = DatabaseItem(this)

        // Lấy thông tin category từ Intent
        category = intent.getStringExtra("CATEGORY") ?: "dong_vat" // Mặc định là "dong_vat"

        // Hiển thị category vào TextView với giá trị thay đổi tùy thuộc vào category
        val categoryTextView: TextView = findViewById(R.id.name_of_category)

        val categoryText = when (category) {
            "dong_vat" -> "Câu hỏi về Động Vật"
            "do_vat" -> "Câu hỏi về Đồ vật"
            "vehicle" -> "Câu hỏi về Xe cộ"
            "quan_ao" -> "Câu hỏi về Trang phục"
            else -> "Câu hỏi về Chưa xác định" // Mặc định nếu không phải các category trên
        }

        categoryTextView.text = categoryText // Hiển thị thông báo vào TextView

        // Lấy dữ liệu hình ảnh trong category được chọn
        val selectedImages = database.getImagesByCategory(category)

        // Chọn ngẫu nhiên 1 ảnh đúng từ danh sách
        correctImage = selectedImages.random()

        // Hiển thị tên vật thể (vietnameseText) lên TextView
        val textView: TextView = findViewById(R.id.textView8)
        textView.text = "Hãy chọn: ${correctImage.vietnameseText}"

        // Xử lý các ImageButton
        val imageButton1: ImageButton = findViewById(R.id.imageButton1)
        val imageButton2: ImageButton = findViewById(R.id.imageButton2)
        val imageButton3: ImageButton = findViewById(R.id.imageButton3)
        val imageButton4: ImageButton = findViewById(R.id.imageButton4)

        // Lấy ảnh ngẫu nhiên từ các category khác để tạo các đáp án sai
        val validCategories = listOf("dong_vat", "do_vat", "quan_ao", "vehicle")
        val otherCategories = validCategories.filter { it != category }
        val randomImages = mutableListOf<ImageRecognition>()

        // Lấy thêm ảnh từ các chủ đề khác
        otherCategories.forEach { cat ->
            val imagesFromCategory = database.getImagesByCategory(cat)
            randomImages.addAll(imagesFromCategory)
        }

        // Đảm bảo số lượng ảnh là đủ
        val allImages = mutableListOf(correctImage)
        allImages.addAll(randomImages.shuffled().take(3))

        // Xáo trộn đáp án
        val shuffledImages = allImages.shuffled()

        // Gán ảnh vào các ImageButton
        val imageButtons = listOf(imageButton1, imageButton2, imageButton3, imageButton4)
        for (i in imageButtons.indices) {
            val imageButton = imageButtons[i]
            val image = shuffledImages[i]

            val assetManager: AssetManager = assets
            try {
                val inputStream: InputStream = assetManager.open(image.imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageButton.setImageBitmap(bitmap) // Đặt hình ảnh vào ImageButton
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Thêm sự kiện click vào các ImageButton
            imageButton.setOnClickListener {
                if (image == correctImage) {
                    Toast.makeText(this, "Đúng rồi!", Toast.LENGTH_SHORT).show()
                    showFireworks()
                } else {
                    playWrongSound()
                    Toast.makeText(this, "Sai rồi, thử lại!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun playQuestion(name: String){
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak(name, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun showFireworks() {
        fireworksAnimation.visibility = View.VISIBLE
        fireworksAnimation.playAnimation()
        fireworksAnimation.translationZ = 10f

        Handler(Looper.getMainLooper()).postDelayed({
            fireworksAnimation.visibility = View.GONE
            val intent = Intent(this, CategoryActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak("Chính xác!", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun playWrongSound() {
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak("Sai rồi!", TextToSpeech.QUEUE_FLUSH, null, null)
    }
}

