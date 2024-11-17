package com.example.myapplication.controller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.controller.detection.BoundingBox
import com.example.myapplication.controller.detection.Constants
import com.example.myapplication.controller.detection.Detector
import android.util.Log
import com.example.myapplication.model.DatabaseItem

class CameraDetectActivity : AppCompatActivity(), Detector.DetectorListener {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var detector: Detector
    private lateinit var database: DatabaseItem
    private val TAG = "CameraDetectActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_detect)

        // Khởi tạo database
        database = DatabaseItem(this)

        imageView = findViewById(R.id.imageViewResult)
        resultTextView = findViewById(R.id.resultTextView)

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
    }


    // Hàm tiền xử lý để tăng cường độ sáng và độ tương phản
    private fun enhanceBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)

        val contrast = 1.5f
        val brightness = 20f

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                var red = (Color.red(pixel) * contrast + brightness).coerceIn(0f, 255f)
                var green = (Color.green(pixel) * contrast + brightness).coerceIn(0f, 255f)
                var blue = (Color.blue(pixel) * contrast + brightness).coerceIn(0f, 255f)

                enhancedBitmap.setPixel(x, y, Color.rgb(red.toInt(), green.toInt(), blue.toInt()))
            }
        }

        return enhancedBitmap
    }

    // Xử lý khi không phát hiện đồ vật nào
    override fun onEmptyDetect() {
        resultTextView.text = "Không phát hiện đồ vật nào"
        Log.d(TAG, "Không phát hiện đồ vật nào")
    }

    // Xử lý khi phát hiện đồ vật
    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        // Hiển thị tất cả nhãn đã phát hiện trong resultTextView
        val resultText = boundingBoxes.joinToString("\n") { it.clsName }
        resultTextView.text = resultText

        // Lấy các View TextView từ layout
        val vn_item = findViewById<TextView>(R.id.vietnamese_item)
        val eng_item = findViewById<TextView>(R.id.english_item)

        // Chuẩn bị nội dung riêng cho mỗi TextView
        val vnText = StringBuilder()
        val engText = StringBuilder()

        // Xử lý từng nhãn
        val detectedLabels = boundingBoxes.map { it.clsName }
        for (label in detectedLabels) {
            val image = database.getImageByLabel(label)
            if (image != null) {
                // Nếu tìm thấy trong cơ sở dữ liệu
                vnText.append("${image.vietnameseText}\n")
                engText.append("${image.englishText}\n")
            } else {
                // Nếu không tìm thấy trong cơ sở dữ liệu
                vnText.append("Không tìm thấy trong cơ sở dữ liệu.\n")
                engText.append("Not found in the database.\n")
            }
        }

        // Cập nhật nội dung cho TextView
        vn_item.text = vnText.toString()
        eng_item.text = engText.toString()
    }


    // Dọn dẹp tài nguyên khi hủy Activity
    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
    }
}
