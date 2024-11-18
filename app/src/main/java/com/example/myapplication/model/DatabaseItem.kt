package com.example.myapplication.model

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

data class ImageRecognition(
    val id: Int,
    val imagePath: String,
    val vietnameseText: String,
    val englishText: String
)

class DatabaseItem(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1 // Tăng version để cập nhật bảng
        private const val DATABASE_NAME = "ImageDatabase.db"
        private const val TABLE_IMAGES = "Images"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE_PATH = "image_path"
        private const val COLUMN_VIETNAMESE_TEXT = "vietnamese"
        private const val COLUMN_ENGLISH_TEXT = "english"
        private const val DATA_FILE_NAME = "data.txt" // Tên file trong thư mục assets
    }

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_IMAGES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IMAGE_PATH TEXT,
                $COLUMN_VIETNAMESE_TEXT TEXT,
                $COLUMN_ENGLISH_TEXT TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)

        // Đọc dữ liệu từ file txt và chèn vào cơ sở dữ liệu
        loadDataFromTxt(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGES")
        onCreate(db)
    }

    private fun loadDataFromTxt(db: SQLiteDatabase) {
        try {
            // Mở file trong thư mục assets
            val inputStream = appContext.assets.open(DATA_FILE_NAME)
            val bufferedReader = inputStream.bufferedReader()

            bufferedReader.useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split(";") // Dữ liệu được phân tách bằng dấu ";"
                    if (parts.size >= 3) { // Đảm bảo đủ dữ liệu
                        val imagePath = parts[0].trim()
                        val vietnameseText = parts[1].trim().replace("_", " ") // Xử lý dấu "_"
                        val englishText = parts[2].trim().replace("_", " ") // Xử lý dấu "_"

                        val values = ContentValues().apply {
                            put(COLUMN_IMAGE_PATH, imagePath)
                            put(COLUMN_VIETNAMESE_TEXT, vietnameseText)
                            put(COLUMN_ENGLISH_TEXT, englishText)
                        }
                        db.insert(TABLE_IMAGES, null, values)
                    }
                }
            }
            Log.d("DatabaseItem", "Dữ liệu từ file txt đã được chèn vào cơ sở dữ liệu.")
        } catch (e: Exception) {
            Log.e("DatabaseItem", "Lỗi khi đọc file txt: ${e.message}")
        }
    }

    fun getAllImages(): List<ImageRecognition> {
        val imagesList = mutableListOf<ImageRecognition>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(TABLE_IMAGES, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH))
                val vietnameseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIETNAMESE_TEXT))
                val englishText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_TEXT))

                val image = ImageRecognition(
                    id = id,
                    imagePath = imagePath,
                    vietnameseText = vietnameseText,
                    englishText = englishText
                )
                imagesList.add(image)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return imagesList
    }

    fun getImageCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_IMAGES", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun getImageByLabel(label: String): ImageRecognition? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_IMAGES,
            null,
            "$COLUMN_ENGLISH_TEXT = ?",
            arrayOf(label.replace("_", " ")), // Xử lý dấu "_" nếu có
            null,
            null,
            null
        )

        var image: ImageRecognition? = null
        if (cursor.moveToFirst()) {
            image = ImageRecognition(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                vietnameseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIETNAMESE_TEXT)),
                englishText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_TEXT))
            )
        }
        cursor.close()
        return image
    }
}
