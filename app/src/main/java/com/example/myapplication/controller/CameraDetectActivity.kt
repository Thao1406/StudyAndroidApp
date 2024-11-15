package com.example.myapplication.controller

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.controller.detection.BoundingBox
import com.example.myapplication.controller.detection.Constants
import com.example.myapplication.controller.detection.Detector
import android.util.Log

class CameraDetectActivity : AppCompatActivity(), Detector.DetectorListener {

    private lateinit var imageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var detector: Detector
    private val TAG = "CameraDetectActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_detect)

        imageView = findViewById(R.id.imageViewResult)
        resultTextView = findViewById(R.id.resultTextView)

        // Nhận ảnh bitmap từ intent
        val photo = intent.getParcelableExtra<Bitmap>("photo")
        if (photo != null) {
            // Hiển thị ảnh gốc trên ImageView
            imageView.setImageBitmap(photo)

            // Áp dụng tiền xử lý: Tăng cường độ sáng và độ tương phản
            val enhancedBitmap = enhanceBitmap(photo)

            // Khởi tạo Detector
            detector = Detector(
                context = this,
                modelPath = Constants.MODEL_PATH,
                labelPath = Constants.LABELS_PATH,
                detectorListener = this
            )
            detector.setup()

            // Chạy nhận diện trên ảnh đã được tiền xử lý
            detector.detect(enhancedBitmap)
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
        val resultText = boundingBoxes.joinToString("\n") { "${it.clsName}: ${it.cnf * 100}%" }
        resultTextView.text = "$resultText"

        Log.d(TAG, "Detection Results:")
        boundingBoxes.forEach { box ->
            Log.d(TAG, "Class: ${box.clsName}, Confidence: ${box.cnf * 100}%")
        }
        Log.d(TAG, "Inference Time: $inferenceTime ms")
    }

    // Dọn dẹp tài nguyên khi hủy Activity
    override fun onDestroy() {
        super.onDestroy()
        detector.clear()
    }
}
