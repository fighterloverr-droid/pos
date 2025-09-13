package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SalesAdapter(private val items: List<SaleItem>) : RecyclerView.Adapter<SalesAdapter.SaleViewHolder>() {

    // ViewHolder: list_item_sale.xml ထဲက UI element တွေကို ကိုင်ထားပေးမယ့် helper class ပါ။
    inner class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemPrice: TextView = itemView.findViewById(R.id.textViewItemPrice)
        val itemTotal: TextView = itemView.findViewById(R.id.textViewItemTotal)
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

        holder.itemName.text = currentItem.name
        holder.itemPrice.text = "${currentItem.quantity} x ${currentItem.price.toInt()} Ks"
        holder.itemTotal.text = "${(currentItem.quantity * currentItem.price).toInt()} Ks"
    }
}