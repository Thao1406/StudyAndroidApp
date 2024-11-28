package com.example.myapplication.controller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.example.myapplication.model.DatabaseAccount
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.result.contract.ActivityResultContracts

class UserinforActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView // Avatar ImageView
    private lateinit var edtName: EditText
    private lateinit var edtBirthday: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtAddress: EditText
    private lateinit var btnSave: Button
    private lateinit var database: DatabaseAccount

    private var avatarUri: Uri? = null // Lưu URI của ảnh được chọn

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userinfor)

        // Khởi tạo các view
        imageView = findViewById(R.id.imageView)
        edtName = findViewById(R.id.edtName)
        edtBirthday = findViewById(R.id.edtBirthday)
        edtEmail = findViewById(R.id.edtEmail)
        edtPhone = findViewById(R.id.edtPhone)
        edtAddress = findViewById(R.id.edtAddress)
        btnSave = findViewById(R.id.btnSave)

        // Khởi tạo database
        database = DatabaseAccount(this)

        // Lấy username từ Intent hoặc SharedPreferences
        val username = intent.getStringExtra("username") ?: database.getLoggedInUser()
        if (username == null) {
            Toast.makeText(this, "Không tìm thấy tài khoản đang đăng nhập!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Kiểm tra và yêu cầu quyền truy cập bộ nhớ
        checkPermissions()

        // Lấy thông tin người dùng từ database
        val userInfo = database.getUserInfo(username)

        if (userInfo != null) {
            // Hiển thị thông tin người dùng trong EditText
            edtName.setText(userInfo.name)
            edtBirthday.setText(userInfo.birthday)
            edtEmail.setText(userInfo.email)
            edtPhone.setText(userInfo.phone)
            edtAddress.setText(userInfo.address)

            // Hiển thị avatar nếu có
            if (userInfo.avatarUri != null) {
                avatarUri = Uri.parse(userInfo.avatarUri)
                displayImage(avatarUri) // Hiển thị ảnh với ContentResolver
            }
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show()
        }

        // Xử lý khi nhấn vào ImageView để chọn ảnh
        imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            launcher.launch(intent)
        }

        // Xử lý khi nhấn nút Lưu
        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val birthday = edtBirthday.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val address = edtAddress.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Họ tên, email và số điện thoại không được để trống!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isSuccess = database.updateUserInfo(
                username = username,
                name = name,
                birthday = birthday,
                email = email,
                phone = phone,
                address = address,
                avatarUri = avatarUri?.toString() // Lưu avatar vào database
            )

            if (isSuccess) {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật thông tin!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    // Launcher để xử lý kết quả khi chọn ảnh
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            avatarUri = result.data?.data
            displayImage(avatarUri) // Hiển thị ảnh với ContentResolver
        }
    }

    private fun displayImage(uri: Uri?) {
        if (uri != null) {
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(bitmap) // Hiển thị ảnh trên ImageView
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Không thể hiển thị ảnh đã chọn.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập bộ nhớ đã được cấp.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
