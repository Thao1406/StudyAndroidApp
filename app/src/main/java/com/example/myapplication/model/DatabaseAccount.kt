package com.example.myapplication.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import android.content.SharedPreferences

class DatabaseAccount(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val TABLE_USERS = "Users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_BIRTHDAY = "birthday"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_AVATAR = "avatar"


        // Keys cho SharedPreferences
        private const val PREFS_NAME = "user_session"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val LOGGED_IN_USER = "loggedInUser"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun onCreate(db: SQLiteDatabase) {
        // Tạo bảng Users
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT,
                $COLUMN_PASSWORD TEXT,
                $COLUMN_NAME TEXT,
                $COLUMN_BIRTHDAY TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_AVATAR TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)

        // Tạo tài khoản test (admin - admin)
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, "admin")
            put(COLUMN_PASSWORD, "admin")
            put(COLUMN_NAME, "Admin User")
            put(COLUMN_BIRTHDAY, "01/01/1990")
            put(COLUMN_EMAIL, "admin@example.com")
            put(COLUMN_PHONE, "0123456789")
            put(COLUMN_ADDRESS, "123 Admin Street")
        }
        db.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // Thêm người dùng mới
    fun addUser(
        username: String,
        password: String,
        name: String = "",
        birthday: String = "",
        email: String = "",
        phone: String = "",
        address: String = "",
        avatarUri: String? = null
    ): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NAME, name)
            put(COLUMN_BIRTHDAY, birthday)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_ADDRESS, address)
            if (avatarUri != null) put(COLUMN_AVATAR, avatarUri)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }



    // Kiểm tra đăng nhập
    fun checkUser(username: String, password: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS, arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME=? AND $COLUMN_PASSWORD=?",
            arrayOf(username, password), null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Lấy thông tin người dùng
    fun getUserInfo(username: String): UserInfo? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS WHERE $COLUMN_USERNAME = ?", arrayOf(username))
        var userInfo: UserInfo? = null

        if (cursor.moveToFirst()) {
            userInfo = UserInfo(
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                birthday = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                avatarUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)) // Lấy đường dẫn ảnh
            )
        }
        cursor.close()
        return userInfo
    }

    // Cập nhật thông tin người dùng
    fun updateUserInfo(
        username: String,
        name: String,
        birthday: String,
        email: String,
        phone: String,
        address: String,
        avatarUri: String? = null
    ): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_BIRTHDAY, birthday)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PHONE, phone)
            put(COLUMN_ADDRESS, address)
            if (avatarUri != null) put(COLUMN_AVATAR, avatarUri)
        }
        val result = db.update(TABLE_USERS, contentValues, "$COLUMN_USERNAME = ?", arrayOf(username))
        db.close()
        return result > 0
    }


    // Lưu trạng thái đăng nhập vào SharedPreferences
    fun saveLoginSession(username: String) {
        val editor = prefs.edit()
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(LOGGED_IN_USER, username)
        editor.apply()
    }

    // Kiểm tra trạng thái đăng nhập
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(IS_LOGGED_IN, false)
    }

    // Lấy tên người dùng đã đăng nhập
    fun getLoggedInUser(): String? {
        return prefs.getString(LOGGED_IN_USER, null)
    }

    // Xóa trạng thái đăng nhập (Đăng xuất)
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }

    fun checkUsernameExists(username: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getAllUserInfor(): List<UserInfo> {
        val userList = mutableListOf<UserInfo>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS", null)

        if (cursor.moveToFirst()) {
            do {
                val user = UserInfo(
                    username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    birthday = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    avatarUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR)) // Có thể null nếu không có avatar
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userList
    }

}

data class UserInfo(
    val username: String,
    val name: String,
    val birthday: String,
    val email: String,
    val phone: String,
    val address: String,
    val avatarUri: String? = null
)
