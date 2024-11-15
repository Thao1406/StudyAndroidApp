package com.example.myapplication.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseItem
import com.example.myapplication.model.ImageRecognition
import java.io.File
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import java.io.FileNotFoundException
import java.io.InputStream

class HomeActivity : AppCompatActivity() {

    private val CAMERA_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val rootView = findViewById<LinearLayout>(R.id.main)
        val cameraButton = findViewById<ImageButton>(R.id.btncamera)
        val userInforButton = findViewById<ImageButton>(R.id.userInformation)
        val gridLayout = findViewById<androidx.gridlayout.widget.GridLayout>(R.id.gridLayout) // Thêm tham chiếu GridLayout

        rootView.setOnApplyWindowInsetsListener { view, insets ->
            // Sử dụng các API tương thích với API 29
            val systemBarsInsets = WindowInsetsCompat.toWindowInsetsCompat(insets).systemWindowInsets

            // Lấy khoảng cách trên và dưới
            val topInset = systemBarsInsets.top
            val bottomInset = systemBarsInsets.bottom

            // Cập nhật phần đệm cho view
            view.updatePadding(
                top = topInset.takeIf { it > 0 } ?: 0,   // Đệm trên cho thanh trạng thái
                bottom = bottomInset.takeIf { it > 0 } ?: 0  // Đệm dưới cho thanh điều hướng
            )
            insets
        }

        val dbHelper = DatabaseItem(this)
        val imageCount = dbHelper.getImageCount()
        Log.d("DatabaseCheck", "Number of images in the database: $imageCount")

        // Cập nhật giao diện với dữ liệu từ SQLite
        updateUI(gridLayout)

        // Xử lý click vào nút camera
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        userInforButton.setOnClickListener {
            val intent = Intent(this, UserinforActivity::class.java)
            startActivity(intent)
        }


    }

    private fun updateUI(gridLayout: androidx.gridlayout.widget.GridLayout) {
        val dbHelper = DatabaseItem(this)
        val images = dbHelper.getAllImages() // Lấy tất cả dữ liệu từ SQLite

        images.forEach { image ->
            // Tạo layout dọc cho từng mục
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = androidx.gridlayout.widget.GridLayout.LayoutParams().apply {
                    width = 0
                    height = LinearLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = androidx.gridlayout.widget.GridLayout.spec(
                        androidx.gridlayout.widget.GridLayout.UNDEFINED, 1f
                    )
                    setMargins(8, 8, 8, 8)
                }
            }

            // Tạo ImageButton để hiển thị hình ảnh
            val imageButton = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, // Đặt chiều rộng là MATCH_PARENT để chiếm toàn bộ không gian khả dụng
                    600 // Chiều cao cố định là 250dp
                ).apply {
                    setMargins(8, 8, 8, 8) // Đặt khoảng trống cho ImageButton
                }
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER // CENTER_CROP để hình ảnh lấp đầy và được cắt phù hợp
                background = ContextCompat.getDrawable(context, R.drawable.outline_back_button)
                // Tải hình ảnh từ assets
                val assetManager: AssetManager = assets
                try {
                    val inputStream: InputStream = assetManager.open(image.imagePath)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    setImageBitmap(bitmap) // Đặt bitmap vào ImageButton
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    Log.e("ImageLoadError", "File not found: ${image.imagePath}")
                }

                // Sự kiện click để mở ItemActivity với dữ liệu hình ảnh
                setOnClickListener {
                    val intent = Intent(this@HomeActivity, ItemActivity::class.java).apply {
                        putExtra("imagePath", image.imagePath)
                        putExtra("vietnameseText", image.vietnameseText)
                        putExtra("englishText", image.englishText)
                    }
                    startActivity(intent)
                }
            }


            // Tạo TextView để hiển thị văn bản tiếng Việt
            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = image.vietnameseText
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))
            }

            // Thêm ImageButton và TextView vào layout của từng mục
            itemLayout.addView(imageButton)
            itemLayout.addView(textView)
            gridLayout.addView(itemLayout) // Thêm layout mục vào GridLayout
        }
    }



    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            val intent = Intent(this, CameraDetectActivity::class.java)
            intent.putExtra("photo", photo)
            startActivity(intent)
        }
    }
}
