package com.shop.pos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ExpensesAdapter(private val items: List<ExpenseItem>) : RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val expenseName: TextView = itemView.findViewById(R.id.textViewExpenseName)
        val expenseDate: TextView = itemView.findViewById(R.id.textViewExpenseDate)
        val expenseAmount: TextView = itemView.findViewById(R.id.textViewExpenseAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val currentItem = items[position]
        val numberFormat = NumberFormat.getNumberInstance(Locale.US)

        holder.expenseName.text = currentItem.name
        holder.expenseDate.text = currentItem.date
        holder.expenseAmount.text = "-${numberFormat.format(currentItem.amount.toInt())} Ks"
    }
}