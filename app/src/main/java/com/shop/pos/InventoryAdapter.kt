package com.shop.pos

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

// Interface ကို ဒီနေရာမှာ ကြေညာပါ
interface InventoryItemListener {
    fun onEditItem(position: Int)
    fun onDeleteItem(position: Int)
}

class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val listener: InventoryItemListener
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemStock: TextView = itemView.findViewById(R.id.textViewStock)
        val itemPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val soldCount: TextView = itemView.findViewById(R.id.textViewSoldCount)
        val costPrice: TextView = itemView.findViewById(R.id.textViewCostPrice)
        val forSaleStatus: TextView = itemView.findViewById(R.id.textViewForSaleStatus)

        private val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)

        init {
            editButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onEditItem(adapterPosition)
                }
            }
            deleteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteItem(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.itemName.text = currentItem.name
        holder.itemStock.text = "လက်ကျန်: ${currentItem.stockQuantity}"
        holder.soldCount.text = "ရောင်းပြီး: ${currentItem.soldQuantity}"
        holder.costPrice.text = "အရင်း: ${numberFormat.format(currentItem.costPrice.toInt())} Ks"
        holder.itemPrice.text = "ရောင်း: ${numberFormat.format(currentItem.price.toInt())} Ks"

        if (currentItem.isForSale) {
            holder.forSaleStatus.text = "အရောင်းတင်ထားသည်"
            holder.forSaleStatus.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            holder.forSaleStatus.text = "မတင်ရသေးပါ"
            holder.forSaleStatus.setTextColor(Color.GRAY)
        }
    }
}