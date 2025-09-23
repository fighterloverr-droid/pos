package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class SalesAdapter(
    private val items: List<SaleItem>,
    private val listener: SalesItemListener
) : RecyclerView.Adapter<SalesAdapter.SaleViewHolder>() {

    // ViewHolder: list_item_sale.xml ထဲက UI element တွေကို ကိုင်ထားပေးမယ့် helper class ပါ။
    inner class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemPrice: TextView = itemView.findViewById(R.id.textViewItemPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)

        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.buttonIncrease)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.buttonDecrease)

        init {
            deleteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDeleteItem(adapterPosition)
                }
            }
            increaseButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onIncreaseQuantity(adapterPosition)
                }
            }
            decreaseButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onDecreaseQuantity(adapterPosition)
                }
            }
        }
    }

    // ViewHolder အသစ်တစ်ခုကို လိုအပ်တဲ့အခါ ဒီ function က တည်ဆောက်ပေးပါတယ်။
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sale, parent, false)
        return SaleViewHolder(view)
    }

    // List ထဲမှာ ပစ္စည်း ဘယ်နှစ်ခုရှိလဲဆိုတာကို ပြောပြပေးပါတယ်။
    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder တစ်ခုကို data နဲ့ ချိတ်ဆက်ပြီး UI မှာ ပြသပေးပါတယ်။
    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.itemName.text = currentItem.name
        holder.itemPrice.text = "${numberFormat.format(currentItem.price.toInt())} Ks per item"
        holder.itemQuantity.text = currentItem.quantity.toString()
    }
}
