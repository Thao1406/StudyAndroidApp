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
    val englishText: String,
    val voiceVietnamesePath: String?,
    val voiceEnglishPath: String?,
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

    private val context: Context = context

    // Danh sách dữ liệu mẫu
    private val sampleData = listOf(
        ImageRecognition(0, "images/Bag_14.jpg", "Cái cặp sách", "Bag", null, null, 0),
        ImageRecognition(0, "images/Bear_29.jpg", "Con Gấu", "Cat", null, null, 0),
        ImageRecognition(0, "images/Book_154.jpg", "Quyển sách", "Book", null, null, 0),
        ImageRecognition(0, "images/Bottle_94.jpg", "Chai nước", "Boottle", null, null, 0),
        ImageRecognition(0, "images/Brown_bear_43.jpg", "Con gấu đen", "Brown_bear", null, null, 0),
        ImageRecognition(0, "images/Bull_34.jpg", "Con bò tót", "Bull", null, null, 0),
        ImageRecognition(0, "images/Butterfly_103.jpg", "Con bướm", "Butterfly", null, null, 0),
        ImageRecognition(0, "images/Camel_19.jpg", "Con lạc đà", "Camel", null, null, 0),
        ImageRecognition(0, "images/Canary_16.jpg", "Chim kim tước", "Canary", null, null, 0),
        ImageRecognition(0, "images/Candy.jpg", "Cái kẹo", "Candy", null, null, 0),
        ImageRecognition(0, "images/Cat_22.jpg", "Con mèo", "Cat", null, null, 0),
        ImageRecognition(0, "images/Caterpillar_81.jpg", "Con sâu bướm", "Caterpillar", null, null, 0),
        ImageRecognition(0, "images/Cattle_42.jpg", "Con bò sừng dài", "Cattle", null, null, 0),
        ImageRecognition(0, "images/Ceiling_light_103.jpg", "Đèn trần", "Ceiling_light", null, null, 0),
        ImageRecognition(0, "images/Centipede_90.jpg", "Con rết", "Centipede", null, null, 0),
        ImageRecognition(0, "images/Chair_1.jpg", "Cái ghế", "Chair", null, null, 0),
        ImageRecognition(0, "images/Cheetah_8.jpg", "Con báo hoa mai", "Cheetah", null, null, 0),
        ImageRecognition(0, "images/Chicken_47.jpg", "Con gà", "Chicken", null, null, 0),
        ImageRecognition(0, "images/Crab_79.jpg", "Con cua", "Crab", null, null, 0),
        ImageRecognition(0, "images/Crocodile_32.jpg", "Con cá sấu", "Crocodile", null, null, 0),
        ImageRecognition(0, "images/Deer_148.jpg", "Con nai", "Deer", null, null, 0),
        ImageRecognition(0, "images/Dog_82.jpg", "Con chó", "Dog", null, null, 0),
        ImageRecognition(0, "images/Duck_98.jpg", "Con vịt", "Duck", null, null, 0),
        ImageRecognition(0, "images/Eagle_49.jpg", "Con chim ưng", "Eagle", null, null, 0),
        ImageRecognition(0, "images/Electric fan_124.jpg", "cái quạt trần", "Electric fan", null, null, 0),
        ImageRecognition(0, "images/Elephant_95.jpg", "Con voi", "Elephant", null, null, 0),
        ImageRecognition(0, "images/Fish_106.jpg", "Con cá", "Fish", null, null, 0),
        ImageRecognition(0, "images/Fox_3.jpg", "Con cáo", "Fox", null, null, 0),
        ImageRecognition(0, "images/Fridge_16.jpg", "Cái tủ lạnh", "Fridge", null, null, 0),
        ImageRecognition(0, "images/Frog_93.jpg", "Con ếch", "Frog", null, null, 0),
        ImageRecognition(0, "images/Giraffe_5.jpg", "Con hươu cao cổ", "Giraffe", null, null, 0),
        ImageRecognition(0, "images/Glass_2.jpg", "Cái cốc nước", "Glass", null, null, 0),
        ImageRecognition(0, "images/Goat_54.jpg", "Con dê", "Goat", null, null, 0),
        ImageRecognition(0, "images/Goldfish_128.jpg", "Con cá vàng", "Goldfish", null, null, 0),
        ImageRecognition(0, "images/Goose_20.jpg", "Con ngỗng", "Goose", null, null, 0),
        ImageRecognition(0, "images/Hamster_3.jpg", "Con chuột Hamter", "Hamster", null, null, 0),
        ImageRecognition(0, "images/Harbor_seal_2.jpg", "Sư tử biển", "Harbor_seal", null, null, 0),
        ImageRecognition(0, "images/Hedgehog_60.jpg", "Con nhím", "Hedgehog", null, null, 0),
        ImageRecognition(0, "images/Hippopotamus_37.jpg", "Con hà mã", "Hippopotamus", null, null, 0),
        ImageRecognition(0, "images/Horse_63.jpg", "Con ngựa", "Horse", null, null, 0),
        ImageRecognition(0, "images/Jaguar_59.jpg", "Báo đốm", "Jaguar", null, null, 0),
        ImageRecognition(0, "images/Jellyfish_57.jpg", "Con sứa", "Jellyfish", null, null, 0),
        ImageRecognition(0, "images/Kangaroo_57.jpg", "Con chuột túi", "Kangaroo", null, null, 0),
        ImageRecognition(0, "images/Koala_49.jpg", "Con gấu túi", "Koala", null, null, 0),
        ImageRecognition(0, "images/Ladybug_24.jpg", "Con bọ rùa", "Ladybug", null, null, 0),
        ImageRecognition(0, "images/Laptop_3.jpg", "Máy tính", "Laptop", null, null, 0),
        ImageRecognition(0, "images/Leopard_72.jpg", "Con báo", "Leopard", null, null, 0),
        ImageRecognition(0, "images/Lion_12.jpg", "Con sư tử", "Lion", null, null, 0),
        ImageRecognition(0, "images/Lizard_13.jpg", "Con thằn lằn", "Lizard", null, null, 0),
        ImageRecognition(0, "images/Lynx_56.jpg", "Con linh miêu", "Lynx", null, null, 0),
        ImageRecognition(0, "images/Magpie_44.jpg", "Chim ác", "Magpie", null, null, 0),
        ImageRecognition(0, "images/Microwave_14.jpg", "Lò vi sóng", "Microwave", null, null, 0),
        ImageRecognition(0, "images/Monkey_8.jpg", "Con khỉ", "Monkey", null, null, 0),
        ImageRecognition(0, "images/Moths_and_butterflies_18.jpg", "Con bướm", "Moths_and_butterflies", null, null, 0),
        ImageRecognition(0, "images/Mouse_100.jpg", "Con chuột", "Mouse", null, null, 0),
        ImageRecognition(0, "images/Mousepad_63.jpg", "Chuột máy tính", "Mousepad", null, null, 0),
        ImageRecognition(0, "images/Mule_37.jpg", "Con lan", "Mule", null, null, 0),
        ImageRecognition(0, "images/Ostrich_136.jpg", "Con đà điểu", "Ostrich", null, null, 0),
        ImageRecognition(0, "images/Otter_52.jpg", "Con rái cá", "Otter", null, null, 0),
        ImageRecognition(0, "images/Owl_28.jpg", "Con cú", "Owl", null, null, 0),
        ImageRecognition(0, "images/Panda_58.jpg", "Con gấu trúc", "Panda", null, null, 0),
        ImageRecognition(0, "images/Parrot_11.jpg", "Con vẹt", "Parrot", null, null, 0),
        ImageRecognition(0, "images/Pen_12.jpg", "Cái bút", "Pen", null, null, 0),
        ImageRecognition(0, "images/Penguin_98.jpg", "Con chim cánh cụt", "Penguin", null, null, 0),
        ImageRecognition(0, "images/Pig_67.jpg", "Con lợn", "Pig", null, null, 0),
        ImageRecognition(0, "images/Polar_bear_98.jpg", "Con gấu trắng", "Polar_bear", null, null, 0),
        ImageRecognition(0, "images/Rabbit_84.jpg", "Con thỏ", "Rabbit", null, null, 0),
        ImageRecognition(0, "images/Raccoon_94.jpg", "Con gấu mèo", "Raccoon", null, null, 0),
        ImageRecognition(0, "images/Raven_54.jpg", "Con quạ", "Raven", null, null, 0),
        ImageRecognition(0, "images/Red_panda_28.jpg", "Con gấu đỏ", "Red_panda", null, null, 0),
        ImageRecognition(0, "images/Rhinoceros_47.jpg", "Con tê giác", "Rhinoceros", null, null, 0),
        ImageRecognition(0, "images/Ruler_18.jpg", "Cái thước", "Ruler", null, null, 0),
        ImageRecognition(0, "images/Scorpion_69.jpg", "Con bọ cạp", "Scorpion", null, null, 0),
        ImageRecognition(0, "images/Sea_lion_84.jpg", "Con sư tử biển", "Sea_lion", null, null, 0),
        ImageRecognition(0, "images/Sea_turtle_19.jpg", "Con rùa biển", "Sea_turtle", null, null, 0),
        ImageRecognition(0, "images/Seahorse_4.jpg", "Con cá ngựa", "Seahorse", null, null, 0),
        ImageRecognition(0, "images/Shark_71.jpg", "Con cá mập", "Shark", null, null, 0),
        ImageRecognition(0, "images/Sheep_46.jpg", "Con cừu", "Sheep", null, null, 0),
        ImageRecognition(0, "images/Shirt_4.jpg", "Cái áo", "Shirt", null, null, 0),
        ImageRecognition(0, "images/Shrimp_8.jpg", "Con tôm", "Shrimp", null, null, 0),
        ImageRecognition(0, "images/Smartphone_76.jpg", "Điện thoại di động", "Smartphone", null, null, 0),
        ImageRecognition(0, "images/Snail_12.jpg", "Con ốc", "Snail", null, null, 0),
        ImageRecognition(0, "images/Snake_93.jpg", "Con rắn", "Snake", null, null, 0),
        ImageRecognition(0, "images/Sparrow_48.jpg", "Con chim sẻ", "Sparrow", null, null, 0),
        ImageRecognition(0, "images/Spider_2.jpg", "Con nhện", "Spider", null, null, 0),
        ImageRecognition(0, "images/Squid_9.jpg", "Con mực", "Squid", null, null, 0),
        ImageRecognition(0, "images/Squirrel_1.jpg", "Con sóc", "Squirrel", null, null, 0),
        ImageRecognition(0, "images/Starfish_66.jpg", "Con sao biển", "Starfish", null, null, 0),
        ImageRecognition(0, "images/Swan_38.jpg", "Con thiên nga", "Swan", null, null, 0),
        ImageRecognition(0, "images/Table_53.jpg", "Cái bàn", "Table", null, null, 0),
        ImageRecognition(0, "images/Tick_57.jpg", "Con bọ", "Tick", null, null, 0),
        ImageRecognition(0, "images/Tiger_22.jpg", "Con hổ", "Tiger", null, null, 0),
        ImageRecognition(0, "images/Tortoise_94.jpg", "Con rùa", "Tortoise", null, null, 0),
        ImageRecognition(0, "images/Trouser_11.jpg", "Cái quần", "Trouser", null, null, 0),
        ImageRecognition(0, "images/Turkey_44.jpg", "Con gà tây", "Turkey", null, null, 0),
        ImageRecognition(0, "images/Turtle_13.jpg", "Con rùa", "Turtle", null, null, 0),
        ImageRecognition(0, "images/TV_19.jpg", "Cái TV", "TV", null, null, 0),
        ImageRecognition(0, "images/Vase_41.jpg", "Lọ hoa", "Vase", null, null, 0),
        ImageRecognition(0, "images/Whale_73.jpg", "Con cá heo", "Whale", null, null, 0),
        ImageRecognition(0, "images/Woodpecker_7.jpg", "Con chim gõ kiến", "Woodpecker", null, null, 0),
        ImageRecognition(0, "images/Worm_7.jpg", "Con giun", "Worm", null, null, 0),
        ImageRecognition(0, "images/Zebra_8.jpg", "Con ngựa vằn", "Zebra", null, null, 0)
    )



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

        // Chèn dữ liệu mẫu vào cơ sở dữ liệu
        insertSampleData(db)
    }


    private fun insertSampleData(db: SQLiteDatabase) {
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
        Log.d("DataLoad", "Inserted ${sampleData.size} sample data entries into the database.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMAGES") // Xóa bảng cũ
        onCreate(db) // Tạo lại bảng và chèn dữ liệu mới
    }

    fun getAllImages(): List<ImageRecognition> {
        val imagesList = mutableListOf<ImageRecognition>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(TABLE_IMAGES, null, null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val imagePathIndex = cursor.getColumnIndex(COLUMN_IMAGE_PATH)
                val vietnameseTextIndex = cursor.getColumnIndex(COLUMN_VIETNAMESE_TEXT)
                val englishTextIndex = cursor.getColumnIndex(COLUMN_ENGLISH_TEXT)
                val voiceVietnamesePathIndex = cursor.getColumnIndex(COLUMN_VOICE_VIETNAMESE_PATH)
                val voiceEnglishPathIndex = cursor.getColumnIndex(COLUMN_VOICE_ENGLISH_PATH)
                val clickCountIndex = cursor.getColumnIndex(COLUMN_CLICK_COUNT)

                // Kiểm tra nếu tất cả các chỉ số cột đều hợp lệ (≥ 0)
                if (idIndex >= 0 && imagePathIndex >= 0 && vietnameseTextIndex >= 0 &&
                    englishTextIndex >= 0 && voiceVietnamesePathIndex >= 0 &&
                    voiceEnglishPathIndex >= 0 && clickCountIndex >= 0) {

                    val image = ImageRecognition(
                        id = cursor.getInt(idIndex),
                        imagePath = cursor.getString(imagePathIndex),
                        vietnameseText = cursor.getString(vietnameseTextIndex),
                        englishText = cursor.getString(englishTextIndex),
                        voiceVietnamesePath = cursor.getString(voiceVietnamesePathIndex),
                        voiceEnglishPath = cursor.getString(voiceEnglishPathIndex),
                        clickCount = cursor.getInt(clickCountIndex)
                    )
                    imagesList.add(image)
                }
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

    fun updateClickCount(imagePath: String, newClickCount: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CLICK_COUNT, newClickCount)
        }
        db.update(TABLE_IMAGES, values, "$COLUMN_IMAGE_PATH=?", arrayOf(imagePath))
        db.close()
    }
}
