package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class SalesHistoryAdapter(private var records: List<SaleRecord>) : RecyclerView.Adapter<SalesHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val saleDate: TextView = itemView.findViewById(R.id.textViewSaleDate)
        val totalAmount: TextView = itemView.findViewById(R.id.textViewTotalAmount)
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
    }

    // Filter လုပ်ထားတဲ့ list အသစ်ကို လက်ခံမယ့် function
    fun updateList(newList: List<SaleRecord>) {
        records = newList
        notifyDataSetChanged()
    }
}