package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

interface PurchaseDetailItemListener {
    fun onDeleteItem(position: Int)
}

class PurchaseDetailAdapter(
    private val items: List<PurchaseDetailItem>,
    private val listener: PurchaseDetailItemListener
) : RecyclerView.Adapter<PurchaseDetailAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val purchaseInfo: TextView = itemView.findViewById(R.id.textViewPurchaseInfo)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDeleteItem)

        init {
            deleteButton.setOnClickListener {
                listener.onDeleteItem(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_purchase_detail, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)
        val priceFormatted = numberFormat.format(currentItem.purchasePrice.toInt())

        holder.itemName.text = currentItem.name
        holder.purchaseInfo.text = "Qty: ${currentItem.quantity} x ${priceFormatted} Ks"
    }
}