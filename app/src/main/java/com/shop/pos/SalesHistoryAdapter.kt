package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

// Filterable interface ကို implement လုပ်ပါ
class SalesHistoryAdapter(private var records: List<SaleRecord>) : RecyclerView.Adapter<SalesHistoryAdapter.ViewHolder>(), Filterable {

    // Filter လုပ်ထားတဲ့ list ကို သီးသန့်သိမ်းထားရန်
    var filteredRecords: List<SaleRecord> = records

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
        return filteredRecords.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentRecord = filteredRecords[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.customerName.text = currentRecord.customerName.ifEmpty { "Cash Sale" }
        holder.saleDate.text = currentRecord.saleDate
        holder.totalAmount.text = "${numberFormat.format(currentRecord.totalAmount.toInt())} Ks"
    }

    // Search filter logic အသစ်
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint?.toString() ?: ""
                filteredRecords = if (charString.isEmpty()) {
                    records
                } else {
                    records.filter {
                        // Customer name (သို့) Date နဲ့ ရှာနိုင်အောင်
                        it.customerName.contains(charString, true) ||
                                it.saleDate.contains(charString, true)
                    }
                }
                return FilterResults().apply { values = filteredRecords }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredRecords = if (results?.values == null)
                    ArrayList()
                else
                    results.values as List<SaleRecord>
                notifyDataSetChanged()
            }
        }
    }
}