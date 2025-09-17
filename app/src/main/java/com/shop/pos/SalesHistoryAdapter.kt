package com.shop.pos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

interface SaleHistoryItemListener {
    fun onCancelSale(position: Int)
    fun onMarkAsPaid(position: Int)
}

class SalesHistoryAdapter(
    private var records: List<SaleRecord>,
    private val listener: SaleHistoryItemListener
) : RecyclerView.Adapter<SalesHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val saleDate: TextView = itemView.findViewById(R.id.textViewSaleDate)
        val totalAmount: TextView = itemView.findViewById(R.id.textViewTotalAmount)
        val cancelButton: ImageButton = itemView.findViewById(R.id.buttonCancelSale)
        val paymentStatus: TextView = itemView.findViewById(R.id.textViewPaymentStatus)
        val markAsPaidButton: Button = itemView.findViewById(R.id.buttonMarkAsPaid)
        val deliveryStatus: TextView = itemView.findViewById(R.id.textViewDeliveryStatus)

        init {
            cancelButton.setOnClickListener { listener.onCancelSale(adapterPosition) }
            markAsPaidButton.setOnClickListener { listener.onMarkAsPaid(adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sale_record, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentRecord = records[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.customerName.text = currentRecord.customerName.ifEmpty { "Cash Sale" }
        holder.saleDate.text = currentRecord.saleDate
        holder.totalAmount.text = "${numberFormat.format(currentRecord.totalAmount.toInt())} Ks"

        // Payment Status Logic
        holder.paymentStatus.text = currentRecord.paymentStatus
        when (currentRecord.paymentStatus) {
            "ငွေရပြီး" -> {
                holder.paymentStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                holder.markAsPaidButton.visibility = View.GONE
            }
            "COD" -> {
                holder.paymentStatus.setTextColor(Color.parseColor("#2196F3")) // Blue
                holder.markAsPaidButton.visibility = View.VISIBLE
            }
            "အကြွေး" -> {
                holder.paymentStatus.setTextColor(Color.parseColor("#F44336")) // Red
                holder.markAsPaidButton.visibility = View.GONE
            }
            else -> {
                holder.paymentStatus.setTextColor(Color.GRAY)
            }
        }

        // Delivery Status Logic
        if (currentRecord.isDelivered) {
            holder.deliveryStatus.text = "(ပို့ဆောင်ပြီး)"
            holder.deliveryStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            holder.deliveryStatus.text = "(မပို့ရသေးပါ)"
            holder.deliveryStatus.setTextColor(Color.GRAY)
        }
    }

    fun updateList(newList: List<SaleRecord>) {
        records = newList
        notifyDataSetChanged()
    }

    fun getRecordAt(position: Int): SaleRecord {
        return records[position]
    }
}