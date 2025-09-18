package com.shop.pos

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {

    // Shop Info
    private lateinit var editTextShopName: EditText
    private lateinit var editTextShopAddress: EditText
    private lateinit var editTextShopPhone: EditText
    private lateinit var buttonSaveShopInfo: Button

    // Security
    private lateinit var switchPinLock: SwitchMaterial
    private lateinit var textViewChangePin: TextView

    // Data Management
    private lateinit var buttonBackupData: Button
    private lateinit var buttonRestoreData: Button
    private lateinit var buttonExportSales: Button
    private lateinit var buttonExportInventory: Button

    // Permission code
    private val STORAGE_PERMISSION_CODE = 101

    // File picker launcher
    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // --- UI element bindings ---
        editTextShopName = findViewById(R.id.editTextShopName)
        editTextShopAddress = findViewById(R.id.editTextShopAddress)
        editTextShopPhone = findViewById(R.id.editTextShopPhone)
        buttonSaveShopInfo = findViewById(R.id.buttonSaveShopInfo)
        switchPinLock = findViewById(R.id.switchPinLock)
        textViewChangePin = findViewById(R.id.textViewChangePin)
        buttonBackupData = findViewById(R.id.buttonBackupData)
        buttonRestoreData = findViewById(R.id.buttonRestoreData)
        buttonExportSales = findViewById(R.id.buttonExportSales)
        buttonExportInventory = findViewById(R.id.buttonExportInventory)

        loadSettings()
        setupListeners()
    }

    private fun setupListeners() {
        buttonSaveShopInfo.setOnClickListener {
            saveShopInfo()
        }
        switchPinLock.setOnCheckedChangeListener { _, isChecked ->
            PinManager.setPinEnabled(this, isChecked)
            val status = if (isChecked) "ဖွင့်လိုက်ပါပြီ" else "ပိတ်လိုက်ပါပြီ"
            Toast.makeText(this, "PIN Lock ကို $status", Toast.LENGTH_SHORT).show()
        }
        textViewChangePin.setOnClickListener {
            showChangePinDialog()
        }
        buttonBackupData.setOnClickListener {
            checkPermissionAndBackup()
        }
        buttonRestoreData.setOnClickListener {
            showRestoreConfirmationDialog()
        }
        buttonExportSales.setOnClickListener {
            Toast.makeText(this, "Export Sales feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        buttonExportInventory.setOnClickListener {
            Toast.makeText(this, "Export Inventory feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE)
        editTextShopName.setText(prefs.getString("SHOP_NAME", ""))
        editTextShopAddress.setText(prefs.getString("SHOP_ADDRESS", ""))
        editTextShopPhone.setText(prefs.getString("SHOP_PHONE", ""))

        switchPinLock.isChecked = PinManager.isPinEnabled(this)
    }

    private fun saveShopInfo() {
        val prefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE).edit()
        prefs.putString("SHOP_NAME", editTextShopName.text.toString())
        prefs.putString("SHOP_ADDRESS", editTextShopAddress.text.toString())
        prefs.putString("SHOP_PHONE", editTextShopPhone.text.toString())
        prefs.apply()
        Toast.makeText(this, "ဆိုင်အချက်အလက်များ သိမ်းဆည်းပြီးပါပြီ", Toast.LENGTH_SHORT).show()
    }

    private fun showChangePinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("PIN နံပါတ် အသစ် ထည့်သွင်းပါ")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
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
        builder.setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun checkPermissionAndBackup() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            backupDatabase()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
            val backupDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PosBackup")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            val backupFile = File(backupDir, "pos_backup.db")
            FileInputStream(dbFile).channel.use { source ->
                FileOutputStream(backupFile).channel.use { destination ->
                    destination.transferFrom(source, 0, source.size())
                    Toast.makeText(this, "Backup saved to Downloads/PosBackup folder", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRestoreConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Data Restore လုပ်ရန်")
            .setMessage("Data restore လုပ်လိုက်ပါက လက်ရှိ data များအားလုံး ပျက်စီးပြီး backup data များဖြင့် အစားထိုးသွားမှာ ဖြစ်ပါတယ်။ ဆက်လုပ်မှာ သေချာလား?")
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

            val inputStream = contentResolver.openInputStream(backupFileUri)
            val outputStream = FileOutputStream(dbFile)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(this, "Restore successful! Restarting app...", Toast.LENGTH_LONG).show()

            val packageManager = packageManager
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            val componentName = intent!!.component
            val mainIntent = Intent.makeRestartActivityTask(componentName)
            startActivity(mainIntent)
            System.exit(0)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}