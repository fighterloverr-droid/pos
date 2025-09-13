package com.shop.pos

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class VoucherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voucher)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Intent ကနေ SaleRecord data ကို လက်ခံရယူပါ
        val saleRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("EXTRA_SALE_RECORD", SaleRecord::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("EXTRA_SALE_RECORD")
        }

        if (saleRecord != null) {
            populateVoucherData(saleRecord)
        }
    }

    private fun populateVoucherData(record: SaleRecord) {
        // UI element တွေကို ရှာပါ
        val textViewDate = findViewById<TextView>(R.id.textViewDate)
        val textViewCustomerInfo = findViewById<TextView>(R.id.textViewCustomerInfo)
        val layoutItemsContainer = findViewById<LinearLayout>(R.id.layoutItemsContainer)
        val textViewSummary = findViewById<TextView>(R.id.textViewSummary)
        val textViewVoucherGrandTotal = findViewById<TextView>(R.id.textViewVoucherGrandTotal)

        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        // Data တွေ ဖြည့်ပါ
        textViewDate.text = "Date: ${record.saleDate}"
        textViewCustomerInfo.text = "Customer: ${record.customerName}, ${record.customerPhone}"

        // Item list ကို dynamically ဖြည့်ပါ
        layoutItemsContainer.removeAllViews() // clean previous views if any
        for (item in record.items) {
            val itemText = "${item.name} (${item.quantity} x ${numberFormat.format(item.price.toInt())})"
            val itemAmount = numberFormat.format((item.quantity * item.price).toInt())

            // Item row တစ်ကြောင်းစာအတွက် layout အသစ်တည်ဆောက်ပါ
            val rowView = LayoutInflater.from(this).inflate(R.layout.list_item_voucher_row, layoutItemsContainer, false)
            rowView.findViewById<TextView>(R.id.textViewItemDescription).text = itemText
            rowView.findViewById<TextView>(R.id.textViewItemAmount).text = itemAmount

            layoutItemsContainer.addView(rowView)
        }

        // Summary ဖြည့်ပါ
        val summaryText = "Subtotal: ${numberFormat.format(record.subtotal.toInt())} Ks\n" +
                "Discount: -${numberFormat.format(record.discount.toInt())} Ks\n" +
                "Delivery: ${numberFormat.format(record.deliveryFee.toInt())} Ks"
        textViewSummary.text = summaryText
        textViewVoucherGrandTotal.text = "TOTAL: ${numberFormat.format(record.totalAmount.toInt())} Ks"
    }
}