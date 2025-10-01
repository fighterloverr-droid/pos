package com.shop.pos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_ITEM = 1

class SalesHistoryAdapter(
    private var items: List<SalesHistoryListItem>,
    private val listener: SaleHistoryItemListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Updated Views
        val rootLayout: LinearLayout = itemView.findViewById(R.id.rootLayout)
        val customerName: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val saleDate: TextView = itemView.findViewById(R.id.textViewSaleDate)
        val totalAmount: TextView = itemView.findViewById(R.id.textViewTotalAmount)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEditSale)
        val cancelButton: ImageButton = itemView.findViewById(R.id.buttonCancelSale)
        val paymentStatus: TextView = itemView.findViewById(R.id.textViewPaymentStatus)
        val deliveryStatus: TextView = itemView.findViewById(R.id.textViewDeliveryStatus)
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateHeader: TextView = itemView.findViewById(R.id.textViewDateHeader)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SalesHistoryListItem.DateHeader -> VIEW_TYPE_HEADER
            is SalesHistoryListItem.SaleItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_date_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sale_record, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val currentItem = items[position]) {
            is SalesHistoryListItem.DateHeader -> {
                (holder as HeaderViewHolder).dateHeader.text = currentItem.date
            }
            is SalesHistoryListItem.SaleItem -> {
                val itemHolder = holder as ItemViewHolder
                val record = currentItem.saleRecord
                val numberFormat = NumberFormat.getNumberInstance(Locale.US)

                itemHolder.customerName.text = record.customerName.ifEmpty { "Cash Sale" }
                itemHolder.saleDate.text = record.saleDate
                itemHolder.totalAmount.text = "${numberFormat.format(record.totalAmount.toInt())} Ks"

                itemHolder.paymentStatus.text = record.paymentStatus
                when (record.paymentStatus) {
                    "ငွေရပြီး" -> itemHolder.paymentStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
                    "COD" -> itemHolder.paymentStatus.setTextColor(Color.parseColor("#2196F3")) // Blue
                    "အကြွေး" -> itemHolder.paymentStatus.setTextColor(Color.parseColor("#F44336")) // Red
                    else -> itemHolder.paymentStatus.setTextColor(Color.GRAY)
                }

                if (record.isDelivered) {
                    itemHolder.deliveryStatus.text = "(ပို့ဆောင်ပြီး)"
                    itemHolder.deliveryStatus.setTextColor(Color.parseColor("#4CAF50"))
                } else {
                    itemHolder.deliveryStatus.text = "(မပို့ရသေးပါ)"
                    itemHolder.deliveryStatus.setTextColor(Color.GRAY)
                }

                // --- Updated Click Listeners ---
                itemHolder.rootLayout.setOnClickListener { listener.onSaleRecordClick(record) }
                itemHolder.editButton.setOnClickListener { listener.onEditSale(record) }
                itemHolder.cancelButton.setOnClickListener { listener.onCancelSale(record) }
            }
        }
    }

    fun updateList(newList: List<SalesHistoryListItem>) {
        items = newList
        notifyDataSetChanged()
    }

    fun getRecordAt(position: Int) : SaleRecord? {
        return (items[position] as? SalesHistoryListItem.SaleItem)?.saleRecord
    }
}