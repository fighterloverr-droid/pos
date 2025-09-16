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


interface PurchaseWorkflowListener {
    fun onMarkAsArrived(position: Int)
}

class PurchasesAdapter(
    private val items: List<PurchaseItem>,
    private val listener: PurchaseWorkflowListener
) : RecyclerView.Adapter<PurchasesAdapter.PurchaseViewHolder>() {

    inner class PurchaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val supplierName: TextView = itemView.findViewById(R.id.textViewSupplierName)
        val purchaseDate: TextView = itemView.findViewById(R.id.textViewPurchaseDate)
        val totalAmount: TextView = itemView.findViewById(R.id.textViewTotalAmount)
        val status: TextView = itemView.findViewById(R.id.textViewStatus)
        val markAsArrivedButton: Button = itemView.findViewById(R.id.buttonMarkAsArrived)

        init {
            markAsArrivedButton.setOnClickListener {
                listener.onMarkAsArrived(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_purchase, parent, false)
        return PurchaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.supplierName.text = currentItem.supplierName
        holder.purchaseDate.text = currentItem.purchaseDate
        holder.totalAmount.text = "${numberFormat.format(currentItem.totalAmount.toInt())} Ks"

        if (currentItem.hasArrived) {
            holder.status.text = "ရောက်ပြီ"
            holder.status.setTextColor(Color.GREEN)
            holder.markAsArrivedButton.visibility = View.GONE
        } else {
            holder.status.text = "မရောက်သေးပါ"
            holder.status.setTextColor(Color.RED)
            holder.markAsArrivedButton.visibility = View.VISIBLE
        }
    }
}