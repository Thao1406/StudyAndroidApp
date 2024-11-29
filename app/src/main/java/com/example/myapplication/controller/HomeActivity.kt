package com.example.myapplication.controller

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.FileProvider
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
import java.io.File
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.PopupWindow
import com.example.myapplication.model.DatabaseAccount
import java.io.FileNotFoundException
import java.io.FileOutputStream
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

        val isLoggedIn = intent.getBooleanExtra("isLoggedIn", false)
        if (isLoggedIn) {
            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
        }

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

        val menuButton = findViewById<ImageButton>(R.id.imageButton2)

        menuButton.setOnClickListener {
            showPopupMenu(it) // Gọi hàm hiển thị popup
        }
    }

    private fun showPopupMenu(anchor: View) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_menu, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val btnLogout = popupView.findViewById<Button>(R.id.btn_logout)
        val btnInfo = popupView.findViewById<Button>(R.id.btn_info)
        val btnQuestion = popupView.findViewById<Button>(R.id.btn_question)

        btnLogout.setOnClickListener {
            Toast.makeText(this, "Đăng xuất", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            logout()
        }

        btnInfo.setOnClickListener {
            Toast.makeText(this, "Thông tin ứng dụng", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
            openAppInfo()
        }

        btnQuestion.setOnClickListener{
            popupWindow.dismiss()
            openCategory()
        }

        // Lấy tọa độ của anchor
        anchor.post {
            val location = IntArray(2)
            anchor.getLocationOnScreen(location)

            val screenWidth = resources.displayMetrics.widthPixels

            // Đo kích thước PopupWindow
            popupView.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED
            )
            val popupWidth = popupView.measuredWidth
            val popupHeight = popupView.measuredHeight

            // Tính toán tọa độ X và Y
            val x = screenWidth - popupWidth - 20 // Căn sát lề phải, thêm padding 20dp nếu cần
            val y = location[1] - popupHeight - 10// Hiển thị ngay phía trên anchor

            // Hiển thị PopupWindow tại vị trí tính toán
            popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
        }
    }


    private fun logout() {
        val databaseHelper = DatabaseAccount(this)
        databaseHelper.logout()
        Toast.makeText(this, "Bạn đã đăng xuất", Toast.LENGTH_SHORT).show()
        // Chuyển về màn hình đăng nhập
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openAppInfo() {
        val intent = Intent(this, AppInforActivity::class.java)
        startActivity(intent)
    }

    private fun openCategory(){
        val intent = Intent(this, CategoryActivity::class.java)
        startActivity(intent)
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

    private lateinit var photoUri: Uri

    private fun openCamera() {
        // Tạo một file tạm để lưu ảnh
        val photoFile = File.createTempFile("photo_${System.currentTimeMillis()}", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", photoFile)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Truyền URI của ảnh qua Intent
            val intent = Intent(this, CameraDetectActivity::class.java)
            intent.putExtra("photoUri", photoUri.toString())
            startActivity(intent)
        }
    }


//    // Lưu Bitmap thành tệp tạm và trả về URI
//    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
//        val file = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
//        try {
//            val outputStream = FileOutputStream(file)
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            outputStream.flush()
//            outputStream.close()
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return Uri.fromFile(file)
//    }

}
