package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomerListAdapter(
    private var customers: List<Customer>,
    private val onCustomerClick: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val phone: TextView = itemView.findViewById(R.id.textViewCustomerPhone)

        init {
            itemView.setOnClickListener {
                onCustomerClick(customers[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customers[position]
        holder.name.text = customer.name
        holder.phone.text = customer.phone
    }

    override fun getItemCount() = customers.size

    fun updateList(newList: List<Customer>) {
        customers = newList
        notifyDataSetChanged()
    }
}
    
