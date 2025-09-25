package com.shop.pos

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.text.NumberFormat
import java.util.Locale

interface InventoryItemListener {
    fun onEditItem(position: Int)
    fun onDeleteItem(position: Int)
}

class InventoryAdapter(
    private val items: List<InventoryItem>,
    private val listener: InventoryItemListener
) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewItem: ImageView = itemView.findViewById(R.id.imageViewItem)
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemCode: TextView = itemView.findViewById(R.id.textViewItemCode)
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

        // Load image using Coil
        if (!currentItem.imageUri.isNullOrEmpty()) {
            holder.imageViewItem.load(Uri.parse(currentItem.imageUri)) {
                crossfade(true)
                placeholder(R.drawable.ic_inventory)
                error(R.drawable.ic_inventory)
            }
        } else {
            holder.imageViewItem.setImageResource(R.drawable.ic_inventory)
        }

        holder.itemName.text = currentItem.name
        holder.itemCode.text = "Code: ${currentItem.code ?: "N/A"}" // <-- null check ထည့်ထားသည်
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
