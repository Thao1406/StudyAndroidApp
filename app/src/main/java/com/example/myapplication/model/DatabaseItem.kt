package com.example.myapplication.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class ImageRecognition(
    val id: Int,
    val imagePath: String,
    val vietnameseText: String,
    val englishText: String,
    val voiceVietnamesePath: String,
    val voiceEnglishPath: String,
    val clickCount: Int
)

class DatabaseItem(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "ImageDatabase.db"
        private const val TABLE_IMAGES = "Images"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE_PATH = "image_path"
        private const val COLUMN_VIETNAMESE_TEXT = "vietnamese"
        private const val COLUMN_ENGLISH_TEXT = "english"
        private const val COLUMN_VOICE_VIETNAMESE_PATH = "voice_vietnamese"
        private const val COLUMN_VOICE_ENGLISH_PATH = "voice_english"
        private const val COLUMN_CLICK_COUNT = "click_count"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_IMAGES ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_IMAGE_PATH TEXT, "
                + "$COLUMN_VIETNAMESE_TEXT TEXT, "
                + "$COLUMN_ENGLISH_TEXT TEXT, "
                + "$COLUMN_VOICE_VIETNAMESE_PATH TEXT, "
                + "$COLUMN_VOICE_ENGLISH_PATH TEXT, "
                + "$COLUMN_CLICK_COUNT INTEGER DEFAULT 0)")
        db.execSQL(createTable)

        // Thêm dữ liệu mẫu vào bảng Images
        val sampleData = listOf(
            ImageRecognition(0, "path/to/sample_image1.jpg", "Con hổ", "Tiger", "path/to/voice_vietnamese1.mp3", "path/to/voice_english1.mp3", 0),
            ImageRecognition(0, "path/to/sample_image2.jpg", "Con mèo", "Cat", "path/to/voice_vietnamese2.mp3", "path/to/voice_english2.mp3", 0),
            ImageRecognition(0, "path/to/sample_image3.jpg", "Con chó", "Dog", "path/to/voice_vietnamese3.mp3", "path/to/voice_english3.mp3", 0),
            ImageRecognition(0, "path/to/sample_image1.jpg", "Con hổ", "Tiger", "path/to/voice_vietnamese1.mp3", "path/to/voice_english1.mp3", 0),
            // Thêm nhiều mục khác nếu cần
        )

        for (item in sampleData) {
            val values = ContentValues().apply {
                put(COLUMN_IMAGE_PATH, item.imagePath)
                put(COLUMN_VIETNAMESE_TEXT, item.vietnameseText)
                put(COLUMN_ENGLISH_TEXT, item.englishText)
                put(COLUMN_VOICE_VIETNAMESE_PATH, item.voiceVietnamesePath)
                put(COLUMN_VOICE_ENGLISH_PATH, item.voiceEnglishPath)
                put(COLUMN_CLICK_COUNT, item.clickCount)
            }
            db.insert(TABLE_IMAGES, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGES")
        onCreate(db)
    }

    fun getAllImages(): List<ImageRecognition> {
        val imagesList = mutableListOf<ImageRecognition>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(TABLE_IMAGES, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val image = ImageRecognition(
                    id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    imagePath = cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_PATH)),
                    vietnameseText = cursor.getString(cursor.getColumnIndex(COLUMN_VIETNAMESE_TEXT)),
                    englishText = cursor.getString(cursor.getColumnIndex(COLUMN_ENGLISH_TEXT)),
                    voiceVietnamesePath = cursor.getString(cursor.getColumnIndex(COLUMN_VOICE_VIETNAMESE_PATH)),
                    voiceEnglishPath = cursor.getString(cursor.getColumnIndex(COLUMN_VOICE_ENGLISH_PATH)),
                    clickCount = cursor.getInt(cursor.getColumnIndex(COLUMN_CLICK_COUNT))
                )
                imagesList.add(image)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return imagesList
    }

    fun updateClickCount(imagePath: String, newClickCount: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CLICK_COUNT, newClickCount)
        }
        db.update(TABLE_IMAGES, values, "$COLUMN_IMAGE_PATH=?", arrayOf(imagePath))
        db.close()
    }
}
