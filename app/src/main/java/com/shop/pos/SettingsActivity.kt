package com.shop.pos

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {

    // License Info
    private lateinit var textViewLicenseStatus: TextView
    private lateinit var buttonActivateNow: Button

    // Shop Info
    private lateinit var editTextShopName: EditText
    private lateinit var editTextShopAddress: EditText
    private lateinit var editTextShopPhone: EditText
    private lateinit var buttonSaveShopInfo: Button

    // Security
    private lateinit var switchPinLock: SwitchMaterial
    private lateinit var textViewChangePin: TextView

    // Data Management
    private lateinit var buttonCustomerList: Button
    private lateinit var buttonBackupData: Button
    private lateinit var buttonRestoreData: Button
    private lateinit var buttonExportSales: Button
    private lateinit var buttonExportInventory: Button

    // Printer Settings
    private lateinit var editTextChars58mm: EditText
    private lateinit var editTextChars80mm: EditText
    private lateinit var buttonSavePrinterInfo: Button

    // Permission code
    private val STORAGE_PERMISSION_CODE = 101

    // Repo instances
    private lateinit var salesRepository: SalesRepository
    private lateinit var inventoryRepository: InventoryRepository

    // File picker launcher
    private val restoreLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.also { uri ->
                    restoreDatabaseFromUri(uri)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val app = application as PosApplication
        salesRepository = SalesRepository(app.database.salesDao())
        inventoryRepository = InventoryRepository(app.database.inventoryDao())

        bindViews()
        loadSettings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateLicenseStatusView()
    }

    private fun bindViews() {
        // License
        textViewLicenseStatus = findViewById(R.id.textViewLicenseStatus)
        buttonActivateNow = findViewById(R.id.buttonActivateNow)
        // Shop Info
        editTextShopName = findViewById(R.id.editTextShopName)
        editTextShopAddress = findViewById(R.id.editTextShopAddress)
        editTextShopPhone = findViewById(R.id.editTextShopPhone)
        buttonSaveShopInfo = findViewById(R.id.buttonSaveShopInfo)
        // Security
        switchPinLock = findViewById(R.id.switchPinLock)
        textViewChangePin = findViewById(R.id.textViewChangePin)
        // Data Management
        buttonCustomerList = findViewById(R.id.buttonCustomerList)
        buttonBackupData = findViewById(R.id.buttonBackupData)
        buttonRestoreData = findViewById(R.id.buttonRestoreData)
        buttonExportSales = findViewById(R.id.buttonExportSales)
        buttonExportInventory = findViewById(R.id.buttonExportInventory)
        // Printer Settings
        editTextChars58mm = findViewById(R.id.editTextChars58mm)
        editTextChars80mm = findViewById(R.id.editTextChars80mm)
        buttonSavePrinterInfo = findViewById(R.id.buttonSavePrinterInfo)
    }

    private fun setupListeners() {
        buttonActivateNow.setOnClickListener {
            startActivity(Intent(this, ActivationActivity::class.java))
        }
        buttonSaveShopInfo.setOnClickListener { saveShopInfo() }
        switchPinLock.setOnCheckedChangeListener { _, isChecked ->
            PinManager.setPinEnabled(this, isChecked)
            val status = if (isChecked) "ဖွင့်လိုက်ပါပြီ" else "ပိတ်လိုက်ပါပြီ"
            Toast.makeText(this, "PIN Lock ကို $status", Toast.LENGTH_SHORT).show()
        }
        textViewChangePin.setOnClickListener { showChangePinDialog() }
        buttonCustomerList.setOnClickListener {
            startActivity(Intent(this, CustomerListActivity::class.java))
        }
        buttonBackupData.setOnClickListener { checkPermissionAndBackup() }
        buttonRestoreData.setOnClickListener { showRestoreConfirmationDialog() }
        buttonExportSales.setOnClickListener {
            checkPermissionAndExport { exportSalesToCsv() }
        }
        buttonExportInventory.setOnClickListener {
            checkPermissionAndExport { exportInventoryToCsv() }
        }
        buttonSavePrinterInfo.setOnClickListener { savePrinterInfo() }
    }

    private fun updateLicenseStatusView() {
        if (LicenseManager.isActivated(this)) {
            textViewLicenseStatus.text = "Status: Full Version Activated"
            textViewLicenseStatus.setTextColor(
                ContextCompat.getColor(this, R.color.primary_teal)
            )
            buttonActivateNow.visibility = View.GONE
        } else {
            val prefs = getSharedPreferences("license_prefs", Context.MODE_PRIVATE)
            val firstRun = prefs.getLong("first_run_timestamp", 0L)

            if (firstRun == 0L) {
                prefs.edit().putLong("first_run_timestamp", System.currentTimeMillis()).apply()
            }

            val elapsedTime = System.currentTimeMillis() - prefs.getLong("first_run_timestamp", System.currentTimeMillis())
            val remainingHours = 24 - TimeUnit.MILLISECONDS.toHours(elapsedTime)

            if (remainingHours > 0) {
                textViewLicenseStatus.text =
                    "Status: Trial Version ($remainingHours hours remaining)"
            } else {
                textViewLicenseStatus.text = "Status: Trial Expired"
            }

            textViewLicenseStatus.setTextColor(
                ContextCompat.getColor(this, android.R.color.holo_red_dark)
            )
            buttonActivateNow.visibility = View.VISIBLE
        }
    }

    private fun loadSettings() {
        // Shop Info
        val prefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE)
        editTextShopName.setText(prefs.getString("SHOP_NAME", ""))
        editTextShopAddress.setText(prefs.getString("SHOP_ADDRESS", ""))
        editTextShopPhone.setText(prefs.getString("SHOP_PHONE", ""))
        switchPinLock.isChecked = PinManager.isPinEnabled(this)

        // Printer Settings
        val printerPrefs = getSharedPreferences("PrinterPrefs", Context.MODE_PRIVATE)
        editTextChars58mm.setText(printerPrefs.getInt("CHARS_58MM", 32).toString())
        editTextChars80mm.setText(printerPrefs.getInt("CHARS_80MM", 48).toString())
    }

    private fun saveShopInfo() {
        val prefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE).edit()
        prefs.putString("SHOP_NAME", editTextShopName.text.toString())
        prefs.putString("SHOP_ADDRESS", editTextShopAddress.text.toString())
        prefs.putString("SHOP_PHONE", editTextShopPhone.text.toString())
        prefs.apply()
        Toast.makeText(this, "ဆိုင်အချက်အလက်များ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
    }

    private fun savePrinterInfo() {
        val printerPrefs = getSharedPreferences("PrinterPrefs", Context.MODE_PRIVATE).edit()
        val chars58 = editTextChars58mm.text.toString().toIntOrNull() ?: 32
        val chars80 = editTextChars80mm.text.toString().toIntOrNull() ?: 48
        printerPrefs.putInt("CHARS_58MM", chars58)
        printerPrefs.putInt("CHARS_80MM", chars80)
        printerPrefs.apply()
        Toast.makeText(this, "Printer settings saved", Toast.LENGTH_SHORT).show()
    }

    private fun showChangePinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("PIN နံပါတ် အသစ် ထည့်သွင်းပါ")
        val input = EditText(this)
        input.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.hint = "ဂဏန်း ၄ လုံး ထည့်ပါ"
        builder.setView(input)
        builder.setPositiveButton("သိမ်းမည်") { dialog, _ ->
            val newPin = input.text.toString()
            if (newPin.length == 4 && newPin.all { it.isDigit() }) {
                PinManager.savePin(this, newPin)
                Toast.makeText(this, "PIN နံပါတ်အသစ် သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "ကျေးဇူးပြု၍ ဂဏန်း ၄ လုံး ထည့်ပါ", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("မလုပ်တော့ပါ") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun checkPermissionAndBackup() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            backupDatabase()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted. Please click the button again.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Storage Permission is denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun backupDatabase() {
        try {
            val dbFile = getDatabasePath("pos_database")
            if (!dbFile.exists()) {
                Toast.makeText(this, "Database not found!", Toast.LENGTH_SHORT).show()
                return
            }
            val backupDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "PosBackup"
            )
            if (!backupDir.exists()) backupDir.mkdirs()
            val backupFile = File(backupDir, "pos_backup.db")
            FileInputStream(dbFile).channel.use { source ->
                FileOutputStream(backupFile).channel.use { destination ->
                    destination.transferFrom(source, 0, source.size())
                }
            }
            Toast.makeText(this, "Backup saved to Downloads/PosBackup folder", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRestoreConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Data Restore လုပ်ရန်")
            .setMessage("Restore လုပ်ပါက လက်ရှိ data အားလုံးကို backup data ဖြင့် အစားထိုးမည်။ ဆက်လုပ်မလား?")
            .setPositiveButton("Restore လုပ်မည်") { dialog, _ ->
                openFilePicker()
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ", null)
            .show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        restoreLauncher.launch(intent)
    }

    private fun restoreDatabaseFromUri(backupFileUri: android.net.Uri) {
        val dbFile = getDatabasePath("pos_database")
        try {
            (application as PosApplication).database.close()
            contentResolver.openInputStream(backupFileUri)?.use { inputStream ->
                FileOutputStream(dbFile).use { outputStream -> inputStream.copyTo(outputStream) }
            }
            AlertDialog.Builder(this)
                .setTitle("Restore Successful")
                .setMessage("Data restore အောင်မြင်ပါသည်။ App ကို restart လုပ်ရန်လိုအပ်ပါသည်။")
                .setPositiveButton("Restart Now") { _, _ ->
                    val pm = packageManager
                    val intent = pm.getLaunchIntentForPackage(packageName)
                    val componentName = intent!!.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    startActivity(mainIntent)
                    System.exit(0)
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissionAndExport(exportAction: () -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        } else {
            exportAction()
        }
    }

    private fun exportSalesToCsv() {
        lifecycleScope.launch {
            val salesRecords = salesRepository.getSaleRecords()
            if (salesRecords.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "No sales data to export.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val csvHeader =
                "Date,Customer,Phone,Address,Items,Subtotal,Discount,Delivery,Total,PaymentType,PaymentStatus,IsDelivered\n"
            val sb = StringBuilder().append(csvHeader)
            salesRecords.forEach { record ->
                val itemsString = record.items?.joinToString(" | ") { "${it.name}(${it.quantity})" }?.replace(",", ";") ?: ""
                sb.append(
                    "\"${record.saleDate}\",\"${record.customerName}\",\"${record.customerPhone}\",\"${record.customerAddress}\",\"$itemsString\",${record.subtotal},${record.discount},${record.deliveryFee},${record.totalAmount},\"${record.paymentType}\",\"${record.paymentStatus}\",${record.isDelivered}\n"
                )
            }
            saveCsvToFile("sales_export.csv", sb.toString())
        }
    }

    private fun exportInventoryToCsv() {
        lifecycleScope.launch {
            val inventoryItems = inventoryRepository.getInventoryItems()
            if (inventoryItems.isEmpty()) {
                runOnUiThread {
                    Toast.makeText(this@SettingsActivity, "No inventory data to export.", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }
            val csvHeader = "ID,Name,Code,ImageURI,StockQuantity,SellingPrice,CostPrice,SoldCount,IsForSale\n"
            val sb = StringBuilder().append(csvHeader)
            inventoryItems.forEach { item ->
                sb.append("${item.id},\"${item.name}\",\"${item.code ?: ""}\",\"${item.imageUri ?: ""}\",${item.stockQuantity},${item.price},${item.costPrice},${item.soldQuantity},${item.isForSale}\n")
            }
            saveCsvToFile("inventory_export.csv", sb.toString())
        }
    }

    private fun saveCsvToFile(fileName: String, content: String) {
        try {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { it.write(content.toByteArray()) }
            runOnUiThread {
                Toast.makeText(this, "Exported to Downloads/$fileName", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}

