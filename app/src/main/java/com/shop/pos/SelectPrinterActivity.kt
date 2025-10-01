package com.shop.pos

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class SelectPrinterActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val pairedDevices = mutableListOf<BluetoothDevice>()
    private var saleRecordToPrint: SaleRecord? = null

    private val requestBluetoothPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                findPairedDevices()
            } else {
                Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_printer)

        saleRecordToPrint = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("PRINT_DATA", SaleRecord::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("PRINT_DATA")
        }

        if (saleRecordToPrint == null) {
            Toast.makeText(this, "No data received to print.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "This device does not support Bluetooth", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.recyclerViewPairedDevices)
        setupRecyclerView()
        checkPermissionsAndFindDevices()
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter(pairedDevices) { device ->
            showPaperWidthSelectionDialog(device)
        }
        recyclerView.adapter = deviceAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showPaperWidthSelectionDialog(device: BluetoothDevice) {
        val paperSizes = arrayOf("58mm", "80mm")
        AlertDialog.Builder(this)
            .setTitle("Select Paper Width")
            .setItems(paperSizes) { dialog, which ->
                val printerPrefs = getSharedPreferences("PrinterPrefs", Context.MODE_PRIVATE)
                val charsPerLine = if (which == 0) {
                    printerPrefs.getInt("CHARS_58MM", 32)
                } else {
                    printerPrefs.getInt("CHARS_80MM", 48)
                }
                printVoucher(device, charsPerLine)
                dialog.dismiss()
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun printVoucher(device: BluetoothDevice, charactersPerLine: Int) {
        Toast.makeText(this, "Printing to ${device.name}...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            var printerService: BluetoothPrinterService? = null
            try {
                printerService = BluetoothPrinterService(device, charactersPerLine)
                printerService.connect()

                val shopPrefs = getSharedPreferences("ShopInfoPrefs", Context.MODE_PRIVATE)
                val shopName = shopPrefs.getString("SHOP_NAME", "ဆိုင် လက်စွဲ") ?: "ဆိုင် လက်စွဲ"
                val shopAddress = shopPrefs.getString("SHOP_ADDRESS", "") ?: ""
                val shopPhone = shopPrefs.getString("SHOP_PHONE", "") ?: ""
                val numberFormat = NumberFormat.getInstance(Locale.US)
                val record = saleRecordToPrint!!

                // --- Print Logic အသစ် ---
                printerService.printTextAsImage(shopName, 30f, true, Paint.Align.CENTER)
                if(shopAddress.isNotEmpty()) printerService.printTextAsImage(shopAddress, alignment = Paint.Align.CENTER)
                if(shopPhone.isNotEmpty()) printerService.printTextAsImage(shopPhone, alignment = Paint.Align.CENTER)
                printerService.feedLine()

                printerService.setAlignLeft()
                printerService.printLine("Date: ${record.saleDate}")
                printerService.printTextAsImage("Customer: ${record.customerName}")
                printerService.printLine("--------------------------------")

                record.items?.forEach { item ->
                    val itemTotal = item.quantity * item.price
                    printerService.printTextAsImage("${item.name} (${item.quantity}x)")
                    printerService.setAlignRight()
                    printerService.printLine("${numberFormat.format(itemTotal.toInt())} Ks")
                    printerService.setAlignLeft()
                }

                printerService.printLine("--------------------------------")
                printerService.setAlignRight()
                printerService.printLine("Subtotal: ${numberFormat.format(record.subtotal.toInt())} Ks")
                printerService.printLine("Discount: -${numberFormat.format(record.discount.toInt())} Ks")
                printerService.printLine("Delivery: ${numberFormat.format(record.deliveryFee.toInt())} Ks")
                printerService.printLine("================================")

                printerService.setFontSize("tall", true)
                printerService.printTwoColumn("TOTAL:", "${numberFormat.format(record.totalAmount.toInt())} Ks")
                printerService.setFontSize("normal", false)

                printerService.setAlignCenter()
                printerService.printLine("================================")
                printerService.feedLine()
                printerService.printTextAsImage("*** ကျေးဇူးတင်ပါသည် ***", alignment = Paint.Align.CENTER)
                printerService.feedLine(3)
                printerService.cut()

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SelectPrinterActivity, "Print job sent!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SelectPrinterActivity, "Printing failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                printerService?.disconnect()
            }
        }
    }

    private fun checkPermissionsAndFindDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestBluetoothPermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
            } else {
                findPairedDevices()
            }
        } else {
            findPairedDevices()
        }
    }

    @SuppressLint("MissingPermission")
    private fun findPairedDevices() {
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_LONG).show()
            return
        }
        val devices = bluetoothAdapter?.bondedDevices
        pairedDevices.clear()
        if (devices != null) {
            pairedDevices.addAll(devices)
        }
        deviceAdapter.notifyDataSetChanged()
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired devices found", Toast.LENGTH_SHORT).show()
        }
    }
}