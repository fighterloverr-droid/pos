package com.shop.pos

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.util.Locale

class VoucherActivity : AppCompatActivity() {

    private lateinit var voucherLayout: LinearLayout
    private lateinit var buttonSave: Button
    private lateinit var buttonShare: Button

    private val STORAGE_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        voucherLayout = findViewById(R.id.voucherLayout)
        buttonSave = findViewById(R.id.buttonSave)
        buttonShare = findViewById(R.id.buttonShare)

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
            checkPermissionAndSave()
        }

        buttonShare.setOnClickListener {
            Toast.makeText(this, "Print feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateVoucherData(record: SaleRecord) {
        val textViewShopName = findViewById<TextView>(R.id.textViewShopName)
        val textViewShopAddress = findViewById<TextView>(R.id.textViewShopAddress)
        val textViewShopPhone = findViewById<TextView>(R.id.textViewShopPhone)

        val prefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE)
        textViewShopName.text = prefs.getString("SHOP_NAME", "My POS Shop")
        textViewShopAddress.text = prefs.getString("SHOP_ADDRESS", "Shop Address")
        textViewShopPhone.text = "Tel: ${prefs.getString("SHOP_PHONE", "-")}"

        val textViewDate = findViewById<TextView>(R.id.textViewDate)
        val textViewCustomerInfo = findViewById<TextView>(R.id.textViewCustomerInfo)
        val layoutItemsContainer = findViewById<LinearLayout>(R.id.layoutItemsContainer)
        val textViewSummary = findViewById<TextView>(R.id.textViewSummary)
        val textViewVoucherGrandTotal = findViewById<TextView>(R.id.textViewVoucherGrandTotal)

        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        textViewDate.text = "Date: ${record.saleDate}"
        textViewCustomerInfo.text = "Customer: ${record.customerName}, ${record.customerPhone}"

        layoutItemsContainer.removeAllViews()
        for (item in record.items) {
            val itemText = "${item.name} (${item.quantity} x ${numberFormat.format(item.price.toInt())})"
            val itemAmount = numberFormat.format((item.quantity * item.price).toInt())

            val rowView = LayoutInflater.from(this).inflate(R.layout.list_item_voucher_row, layoutItemsContainer, false)
            rowView.findViewById<TextView>(R.id.textViewItemDescription).text = itemText
            rowView.findViewById<TextView>(R.id.textViewItemAmount).text = itemAmount

            layoutItemsContainer.addView(rowView)
        }

        val summaryText = "Subtotal: ${numberFormat.format(record.subtotal.toInt())} Ks\n" +
                "Discount: -${numberFormat.format(record.discount.toInt())} Ks\n" +
                "Delivery: ${numberFormat.format(record.deliveryFee.toInt())} Ks"
        textViewSummary.text = summaryText
        textViewVoucherGrandTotal.text = "TOTAL: ${numberFormat.format(record.totalAmount.toInt())} Ks"
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun checkPermissionAndSave() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            saveVoucher()
        }
    }

    private fun saveVoucher() {
        val bitmap = getBitmapFromView(voucherLayout)
        if (bitmap != null) {
            saveBitmapToGallery(bitmap)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveVoucher()
            } else {
                Toast.makeText(this, "Storage Permission is denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "Voucher_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentResolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { contentResolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                runOnUiThread {
                    Toast.makeText(this, "ဘောင်ချာကို Gallery ထဲမှာ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Error saving image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}