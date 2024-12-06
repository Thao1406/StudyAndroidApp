package com.example.myapplication.controller

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
    private lateinit var Wronganimation : LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // Cho phép hiển thị chế độ "edge-to-edge"
        setContentView(R.layout.activity_question2)
        fireworksAnimation = findViewById(R.id.fireworksAnimation)
        Wronganimation = findViewById(R.id.Wronganimation)

        // Áp dụng window insets listener để đảm bảo các padding cho hệ thống
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("vi", "VN")
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

        // Hiển thị category vào TextView
        val categoryTextView: TextView = findViewById(R.id.name_of_category)
        val categoryText = when (category) {
            "dong_vat" -> "Câu hỏi về Động Vật"
            "do_vat" -> "Câu hỏi về Đồ vật"
            "vehicle" -> "Câu hỏi về Xe cộ"
            "quan_ao" -> "Câu hỏi về Trang phục"
            else -> "Câu hỏi về Chưa xác định"
        }
        categoryTextView.text = categoryText

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
        val wrongImages = selectedImages.filter { it != correctImage }.shuffled().take(3)

        val allImages = mutableListOf(correctImage)
        allImages.addAll(wrongImages)

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
            loadNewQuestion() // Gọi hàm để thay đổi câu hỏi sau 2 giây
        }, 4000)

        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak("Chính xác!", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun loadNewQuestion() {
        // Lấy lại danh sách câu hỏi từ database và chọn ngẫu nhiên 1 câu hỏi mới
        val selectedImages = database.getImagesByCategory(category)
        correctImage = selectedImages.random()

        // Hiển thị câu hỏi mới lên TextView
        val textView: TextView = findViewById(R.id.textView8)
        textView.text = "Hãy chọn: ${correctImage.vietnameseText}"
        textToSpeech.language = Locale("vi", "VN")
        val questionText = "Hãy chọn ${correctImage.vietnameseText}"
        playQuestion(questionText)

        // Cập nhật lại các đáp án (ImageButton)
        val imageButton1: ImageButton = findViewById(R.id.imageButton1)
        val imageButton2: ImageButton = findViewById(R.id.imageButton2)
        val imageButton3: ImageButton = findViewById(R.id.imageButton3)
        val imageButton4: ImageButton = findViewById(R.id.imageButton4)

        val wrongImages = selectedImages.filter { it != correctImage }.shuffled().take(3)
        val allImages = mutableListOf(correctImage)
        allImages.addAll(wrongImages)
        val shuffledImages = allImages.shuffled()

        val imageButtons = listOf(imageButton1, imageButton2, imageButton3, imageButton4)
        for (i in imageButtons.indices) {
            val imageButton = imageButtons[i]
            val image = shuffledImages[i]

            val assetManager: AssetManager = assets
            try {
                val inputStream: InputStream = assetManager.open(image.imagePath)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageButton.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Cập nhật sự kiện click cho các ImageButton
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

    private fun playWrongSound() {
        Wronganimation.visibility = View.VISIBLE
        Wronganimation.playAnimation()
        Wronganimation.translationZ = 10f
        Handler(Looper.getMainLooper()).postDelayed({
            Wronganimation.visibility = View.GONE
        }, 3000)
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak("Sai rồi!", TextToSpeech.QUEUE_FLUSH, null, null)
    }
}


