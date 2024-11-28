package com.example.myapplication.controller

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseItem
import java.util.Locale

class QuestionActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private lateinit var database: DatabaseItem
    private val handler = Handler() // Handler tương thích với API 29
    private lateinit var fireworksAnimation: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_question)

        fireworksAnimation = findViewById(R.id.fireworksAnimation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale("vi", "VN") // Đặt ngôn ngữ phát âm
                val questionText = "Hình bạn vừa chọn là thứ gì ? "
                playQuestion(questionText) // Gọi phát âm sau khi TextToSpeech đã sẵn sàng
            } else {
                Toast.makeText(this, "Không thể khởi tạo TextToSpeech!", Toast.LENGTH_SHORT).show()
            }
        }

        // Khởi tạo DatabaseItem
        database = DatabaseItem(this)

        // Nhận nhãn đúng từ Intent
        val correctAnswerEnglish = intent.getStringExtra("correctAnswer") ?: "Unknown"

        // Lấy giá trị tiếng Việt từ cơ sở dữ liệu
        val correctAnswer = database.getAllImages()
            .firstOrNull { it.englishText == correctAnswerEnglish } // Tìm đối tượng có EnglishText trùng khớp
            ?.vietnameseText ?: "Không xác định" // Nếu tìm thấy, lấy VietnameseText; nếu không, trả về "Không xác định"


        // Lấy đáp án sai từ database
        val incorrectAnswers = database.getAllImages()
            .map { it.vietnameseText }
            .filter { it != correctAnswer }
            .shuffled()
            .take(2) // Lấy 2 đáp án sai

        // Kết hợp đáp án đúng và sai, sau đó trộn
        val options = listOf(correctAnswer) + incorrectAnswers
        val shuffledOptions = options.shuffled()

        // Ánh xạ các view trong layout
        val questionText = findViewById<TextView>(R.id.questionText)
        val answerButton1 = findViewById<Button>(R.id.answerButton1)
        val answerButton2 = findViewById<Button>(R.id.answerButton2)
        val answerButton3 = findViewById<Button>(R.id.answerButton3)

        questionText.text = "Hình bạn vừa chọn là thứ gì ? "
        answerButton1.text = shuffledOptions[0]
        answerButton2.text = shuffledOptions[1]
        answerButton3.text = shuffledOptions[2]

        val listener = View.OnClickListener { view ->
            val selectedAnswer = (view as Button).text.toString()
            if (selectedAnswer == correctAnswer) {
                // Hiển thị viền xanh cho đáp án đúng
                highlightButton(view, true)
                showFireworks()
            } else {
                // Hiển thị viền đỏ cho đáp án sai
                highlightButton(view, false)
                playWrongSound()
            }
        }

        answerButton1.setOnClickListener(listener)
        answerButton2.setOnClickListener(listener)
        answerButton3.setOnClickListener(listener)
    }

    private fun highlightButton(button: Button, isCorrect: Boolean) {
        val originalBackground = button.background
        val borderColor = if (isCorrect) Color.GREEN else Color.RED

        // Tạo viền màu cho nút
        val drawable = GradientDrawable().apply {
            setColor(Color.TRANSPARENT) // Màu nền nút
            setStroke(5, borderColor)  // Độ dày và màu viền
        }

        // Đặt viền màu
        button.background = drawable

        // Đặt lại trạng thái ban đầu sau 1 giây
        handler.postDelayed({
            button.background = originalBackground
        }, 1000) // 1 giây
    }

    private fun showFireworks() {
        fireworksAnimation.visibility = View.VISIBLE
        fireworksAnimation.playAnimation()
        fireworksAnimation.translationZ = 10f

        Handler(Looper.getMainLooper()).postDelayed({
            fireworksAnimation.visibility = View.GONE
            val intent = Intent(this, HomeActivity::class.java)
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

    private fun playQuestion(name: String){
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak(name, TextToSpeech.QUEUE_FLUSH, null, null)
    }
    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.shutdown()
    }
}
