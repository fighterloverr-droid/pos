package com.shop.pos

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

class SalesAdapter(
    private val items: List<SaleItem>,
    private val listener: SalesItemListener
) : RecyclerView.Adapter<SalesAdapter.SaleViewHolder>() {

    inner class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemThumbnail: ImageView = itemView.findViewById(R.id.imageViewItemThumbnail)
        val itemName: TextView = itemView.findViewById(R.id.textViewItemName)
        val itemPrice: TextView = itemView.findViewById(R.id.textViewItemPrice)
        val itemQuantity: TextView = itemView.findViewById(R.id.textViewQuantity)

        private val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
        private val increaseButton: ImageButton = itemView.findViewById(R.id.buttonIncrease)
        private val decreaseButton: ImageButton = itemView.findViewById(R.id.buttonDecrease)

        init {
            // Safe position check to prevent crashes
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        // Load image using Coil library
        if (!currentItem.imageUri.isNullOrEmpty()) {
            // If there is an image URI, load it
            holder.itemThumbnail.load(Uri.parse(currentItem.imageUri)) {
                crossfade(true) // Smooth transition
                placeholder(R.drawable.ic_inventory) // Show a default icon while loading
                error(R.drawable.ic_inventory) // Show a default icon if loading fails
            }
        } else {
            // If there is no image URI, show the default inventory icon
            holder.itemThumbnail.setImageResource(R.drawable.ic_inventory)
        }

        holder.itemName.text = currentItem.name
        holder.itemPrice.text = "${numberFormat.format(currentItem.price.toInt())} Ks per item"
        holder.itemQuantity.text = currentItem.quantity.toString()
    }
}

