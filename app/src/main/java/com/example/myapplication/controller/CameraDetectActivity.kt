package com.example.myapplication.controller

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.controller.detection.BoundingBox
import com.example.myapplication.controller.detection.Constants
import com.example.myapplication.controller.detection.Detector
import com.example.myapplication.model.DatabaseItem
import java.util.*

class CameraDetectActivity : AppCompatActivity(), Detector.DetectorListener {

    private lateinit var imageView: ImageView
    private lateinit var detector: Detector
    private lateinit var database: DatabaseItem
    private lateinit var textToSpeech: TextToSpeech

    private var clickCount = 0
    private lateinit var boundingBoxes: List<BoundingBox>
    private val boxClickCount = mutableMapOf<String, Int>() // Đếm số lần click vào từng vật thể

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_detect)

        // Khởi tạo database
        database = DatabaseItem(this)

        // Khởi tạo TTS
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US // Đặt ngôn ngữ mặc định là tiếng Anh
            }
        }

        imageView = findViewById(R.id.imageViewResult)

        // Nhận URI từ Intent
        val photoUriString = intent.getStringExtra("photoUri")
        if (photoUriString != null) {
            val photoUri = Uri.parse(photoUriString)
            val photo = BitmapFactory.decodeStream(contentResolver.openInputStream(photoUri))
            if (photo != null) {
                // Hiển thị ảnh gốc trên ImageView
                imageView.setImageBitmap(photo)

                // Áp dụng tiền xử lý và các bước tiếp theo
                val enhancedBitmap = enhanceBitmap(photo)

                // Khởi tạo và chạy nhận diện
                detector = Detector(
                    context = this,
                    modelPath = Constants.MODEL_PATH,
                    labelPath = Constants.LABELS_PATH,
                    detectorListener = this
                )
                detector.setup()
                detector.detect(enhancedBitmap)
            }
        }

        // Xử lý sự kiện click trên ảnh
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!::boundingBoxes.isInitialized) {
                    // Nếu boundingBoxes chưa được khởi tạo, trả về false để ngừng xử lý
                    return@setOnTouchListener false
                }
                val x = event.x / imageView.width
                val y = event.y / imageView.height

                // Kiểm tra nếu click vào bounding box nào
                for (box in boundingBoxes) {
                    if (x >= box.x1 && x <= box.x2 && y >= box.y1 && y <= box.y2) {
                        val label = box.clsName

                        // Phát âm thanh
                        playSoundForBox(label)

                        // Đếm số lần click
                        boxClickCount[label] = boxClickCount.getOrDefault(label, 0) + 1
                        clickCount++

                        // Kiểm tra nếu đạt 5 click
                        if (clickCount >= 5) {
                            val mostClickedLabel = boxClickCount.maxByOrNull { it.value }?.key
                            val clickedBox = boundingBoxes.firstOrNull { it.clsName == mostClickedLabel }
                            goToQuestionActivity(clickedBox)
                        }
                        break
                    }
                }
            }
            true
        }
    }

    private fun drawBoundingBoxes(bitmap: Bitmap, boxes: List<BoundingBox>): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Paint cho bounding box
        val boxPaint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 8f // Độ dày của viền
        }

        // Paint cho nhãn
        val textPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 48f // Kích thước chữ
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        for (box in boxes) {
            val left = box.x1 * bitmap.width
            val top = box.y1 * bitmap.height
            val right = box.x2 * bitmap.width
            val bottom = box.y2 * bitmap.height

            // Vẽ bounding box
            canvas.drawRect(left, top, right, bottom, boxPaint)

            // Vẽ nhãn
            val label = box.clsName // Nhãn của đối tượng
            canvas.drawText(label, left + 10, top - 10, textPaint) // Hiển thị nhãn phía trên bên trái box
        }

        return mutableBitmap
    }

    private fun playSoundForBox(label: String) {
        val vietnameseSound = database.getImageByLabel(label)?.vietnameseText ?: "Không có dữ liệu"
        val englishSound = database.getImageByLabel(label)?.englishText ?: "No data"

        // Phát âm thanh tiếng Việt
        textToSpeech.language = Locale("vi", "VN")
        textToSpeech.speak(vietnameseSound, TextToSpeech.QUEUE_FLUSH, null, null)
        Handler(Looper.getMainLooper()).postDelayed({
            // Phát âm thanh tiếng Anh sau tiếng Việt
            textToSpeech.language = Locale.US
            textToSpeech.speak(englishSound, TextToSpeech.QUEUE_ADD, null, null)
        }, 2000)

    }

    private fun goToQuestionActivity(box: BoundingBox?) {
        val intent = Intent(this, QuestionActivity::class.java)
        intent.putExtra("correctAnswer", box?.clsName) // Truyền nhãn đối tượng đúng
        startActivity(intent)
    }

    private fun enhanceBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)

        val contrast = 1.5f
        val brightness = 20f

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val red = (Color.red(pixel) * contrast + brightness).coerceIn(0f, 255f)
                val green = (Color.green(pixel) * contrast + brightness).coerceIn(0f, 255f)
                val blue = (Color.blue(pixel) * contrast + brightness).coerceIn(0f, 255f)

                enhancedBitmap.setPixel(x, y, Color.rgb(red.toInt(), green.toInt(), blue.toInt()))
            }
        }

        return enhancedBitmap
    }

    override fun onEmptyDetect() {
        // Thông báo khi không phát hiện vật thể nàoqqqw
        runOnUiThread {
            Toast.makeText(this, "Không phát hiện vật thể nào!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        this.boundingBoxes = boundingBoxes

        // Vẽ bounding boxes lên ảnh
        val photo = (imageView.drawable as BitmapDrawable).bitmap
        val detectedBitmap = drawBoundingBoxes(photo, boundingBoxes)
        imageView.setImageBitmap(detectedBitmap)
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
        textToSpeech.shutdown()
    }
}
