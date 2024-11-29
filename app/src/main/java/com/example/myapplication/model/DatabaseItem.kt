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
    val category: String
)

class DatabaseItem(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3 // Tăng version để cập nhật bảng
        private const val DATABASE_NAME = "ImageDatabase.db"
        private const val TABLE_IMAGES = "Images"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IMAGE_PATH = "image_path"
        private const val COLUMN_VIETNAMESE_TEXT = "vietnamese"
        private const val COLUMN_ENGLISH_TEXT = "english"
        private const val COLUMN_CATEGORY = "category"
        private const val DATA_FILE_NAME = "data.txt" // Tên file trong thư mục assets
    }

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_IMAGES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_IMAGE_PATH TEXT,
                $COLUMN_VIETNAMESE_TEXT TEXT,
                $COLUMN_ENGLISH_TEXT TEXT,
                $COLUMN_CATEGORY TEXT
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
        val inputStream = appContext.assets.open(DATA_FILE_NAME)
        val reader = inputStream.bufferedReader()
        val contentValues = ContentValues()

        db.beginTransaction() // Bắt đầu giao dịch để cải thiện hiệu suất

        reader.useLines { lines ->
            lines.forEach { line ->
                val parts = line.split(";") // Tách các giá trị theo dấu ";"

                if (parts.size == 4) {
                    // Đảm bảo rằng bạn có đủ 4 phần tử trong mỗi dòng
                    contentValues.clear()
                    contentValues.put(COLUMN_IMAGE_PATH, parts[0].trim())
                    contentValues.put(COLUMN_VIETNAMESE_TEXT, parts[1].trim())
                    contentValues.put(COLUMN_ENGLISH_TEXT, parts[2].trim())
                    contentValues.put(COLUMN_CATEGORY, parts[3].trim())

                    db.insert(TABLE_IMAGES, null, contentValues)
                }
            }
        }

        db.setTransactionSuccessful() // Đánh dấu giao dịch là thành công
        db.endTransaction() // Kết thúc giao dịch
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
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))

                val image = ImageRecognition(
                    id = id,
                    imagePath = imagePath,
                    vietnameseText = vietnameseText,
                    englishText = englishText,
                    category = category
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
            arrayOf(
                COLUMN_ID,
                COLUMN_IMAGE_PATH,
                COLUMN_VIETNAMESE_TEXT,
                COLUMN_ENGLISH_TEXT,
                COLUMN_CATEGORY // Lấy thêm cột category
            ),
            "$COLUMN_ENGLISH_TEXT = ?", // Truy vấn theo label (có thể là tên tiếng Anh của hình ảnh)
            arrayOf(label),
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
                englishText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_TEXT)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)) // Trả về category
            )
        }
        cursor.close()
        return image
    }

    fun getImagesByCategory(category: String): List<ImageRecognition> {
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_IMAGES,
            arrayOf(COLUMN_ID, COLUMN_IMAGE_PATH, COLUMN_VIETNAMESE_TEXT, COLUMN_ENGLISH_TEXT, COLUMN_CATEGORY),
            "$COLUMN_CATEGORY = ?",
            arrayOf(category),
            null, null, null
        )

        val images = mutableListOf<ImageRecognition>()
        if (cursor.moveToFirst()) {
            do {
                val image = ImageRecognition(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                    vietnameseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIETNAMESE_TEXT)),
                    englishText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_TEXT)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                )
                images.add(image)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return images
    }

    // Truy vấn hình ảnh ngẫu nhiên từ một chủ đề khác
    fun getRandomImagesFromOtherCategories(excludeCategory: String): List<ImageRecognition> {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_IMAGES,  // Tên bảng
            null,  // Lấy tất cả cột
            "$COLUMN_CATEGORY != ?",  // Điều kiện loại bỏ category
            arrayOf(excludeCategory),  // Giá trị của excludeCategory
            null,  // Không cần GROUP BY
            null,  // Không cần HAVING
            "RANDOM()",  // Sắp xếp ngẫu nhiên
            "3"  // Lấy tối đa 3 bản ghi
        )

        val images = mutableListOf<ImageRecognition>()
        if (cursor.moveToFirst()) {
            do {
                val image = ImageRecognition(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_PATH)),
                    vietnameseText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIETNAMESE_TEXT)),
                    englishText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH_TEXT)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                )
                images.add(image)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return images
    }
}
