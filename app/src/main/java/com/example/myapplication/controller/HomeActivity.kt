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

        rootView.setOnApplyWindowInsetsListener { view, insets ->
            val systemBarsInsets = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            } else {
                insets.systemWindowInsetBottom
            }

            view.updatePadding(bottom = systemBarsInsets.takeIf { it > 0 } ?: 0)
            insets
        }

        // Cập nhật giao diện với dữ liệu từ SQLite
        updateUI(rootView)

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

    private fun updateUI(rootView: LinearLayout) {
        val dbHelper = DatabaseItem(this)
        val images = dbHelper.getAllImages() // Lấy tất cả dữ liệu từ SQLite

        images.forEach { image ->
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val imageButton = ImageButton(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    250
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.FIT_CENTER // Sửa lỗi: dùng ImageView thay vì ImageButton

                val imagePath = image.imagePath
                val drawable: Drawable? = Drawable.createFromPath(File(imagePath).absolutePath)
                setImageDrawable(drawable)

                setOnClickListener {
                    val intent = Intent(this@HomeActivity, ItemActivity::class.java)
                    intent.putExtra("imagePath", imagePath)
                    intent.putExtra("vietnameseText", image.vietnameseText)
                    intent.putExtra("englishText", image.englishText)
                    startActivity(intent)
                }
            }


            val textView = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                text = image.vietnameseText
                setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.black))
            }

            layout.addView(imageButton)
            layout.addView(textView)
            rootView.addView(layout)
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
