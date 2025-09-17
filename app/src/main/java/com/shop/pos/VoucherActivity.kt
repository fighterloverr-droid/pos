package com.shop.pos

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.util.Locale

class VoucherActivity : AppCompatActivity() {

    private lateinit var voucherLayout: LinearLayout
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        voucherLayout = findViewById(R.id.voucherLayout)
        buttonSave = findViewById(R.id.buttonSave)

        val saleRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_SALE_RECORD", SaleRecord::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_SALE_RECORD")
        }

        if (saleRecord != null) {
            populateVoucherData(saleRecord)
        }

        buttonSave.setOnClickListener {
            val bitmap = getBitmapFromView(voucherLayout)
            if (bitmap != null) {
                saveBitmapToGallery(bitmap)
            }
        }
    }

    private fun populateVoucherData(record: SaleRecord) {
        // ... (this function is the same as before)
    }

    // --- Function အသစ်များ ---

    /**
     * View (Layout) တစ်ခုကို Bitmap (ပုံ) အဖြစ် ပြောင်းလဲပေးမယ့် function
     */
    private fun getBitmapFromView(view: View): Bitmap? {
        // View ရဲ့ dimension အတိုင်း Bitmap အလွတ်တစ်ခု ဖန်တီးပါ
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        // Canvas ကို အဲ့ဒီ bitmap ပေါ်မှာ တည်ဆောက်ပါ
        val canvas = Canvas(bitmap)
        // View ကို canvas ပေါ်မှာ ဆွဲပါ
        view.draw(canvas)
        return bitmap
    }

    /**
     * Bitmap ကို ဖုန်းရဲ့ Gallery ထဲက Pictures folder ထဲမှာ သိမ်းဆည်းပေးမယ့် function
     */
    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "Voucher_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10 နဲ့ အထက်အတွက်
            val contentResolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            // Android 9 နဲ့ အောက်အတွက်
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            // Bitmap ကို JPEG format နဲ့ compress လုပ်ပြီး သိမ်းဆည်းပါ
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            runOnUiThread {
                Toast.makeText(this, "ဘောင်ချာကို Gallery ထဲမှာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}