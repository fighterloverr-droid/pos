package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// 1. Interface အသစ်တစ်ခု ကြေညာပါ
interface SalesItemListener {
    fun onDeleteItem(position: Int)
    fun onIncreaseQuantity(position: Int)
    fun onDecreaseQuantity(position: Int)
}

class SalesAdapter(
    private val items: List<SaleItem>,
    // 2. Listener ကို constructor ကနေ လက်ခံပါ
    private val listener: SalesItemListener
) : RecyclerView.Adapter<SalesAdapter.SaleViewHolder>() {

    inner class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemPrice: TextView = itemView.findViewById(R.id.textViewItemPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)

        // 3. Button တွေကို ViewHolder ထဲမှာ ရှာဖွေချိတ်ဆက်ပါ
        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.buttonIncrease)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.buttonDecrease)

        init {
            // 4. Button တွေကို နှိပ်လိုက်ရင် Interface ကနေ Fragment ကို ပြန်လှမ်းခေါ်ပါ
            deleteButton.setOnClickListener {
                listener.onDeleteItem(adapterPosition)
            }
            increaseButton.setOnClickListener {
                listener.onIncreaseQuantity(adapterPosition)
            }
            decreaseButton.setOnClickListener {
                listener.onDecreaseQuantity(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val currentItem = items[position]

        holder.itemName.text = currentItem.name
        holder.itemPrice.text = "${currentItem.price.toInt()} Ks per item"
        holder.itemQuantity.text = currentItem.quantity.toString()
    }
}