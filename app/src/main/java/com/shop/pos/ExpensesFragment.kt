package com.shop.pos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpensesFragment : Fragment(), ExpenseItemListener {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var fabAddExpense: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        expensesRecyclerView = view.findViewById(R.id.recyclerViewExpenses)
        fabAddExpense = view.findViewById(R.id.fabAddExpense)

        expensesAdapter = ExpensesAdapter(ExpensesRepository.getExpenseItems(), this)
        expensesRecyclerView.adapter = expensesAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        expensesAdapter.notifyDataSetChanged()
    }

    override fun onEditItem(position: Int) {
        val intent = Intent(requireContext(), AddExpenseActivity::class.java)
        intent.putExtra("EDIT_EXPENSE_POSITION", position)
        startActivity(intent)
    }

    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("မှတ်တမ်း ဖျက်ရန်")
            .setMessage("ဒီကုန်ကျစရိတ်မှတ်တမ်းကို ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                ExpensesRepository.deleteExpenseItem(position)
                expensesAdapter.notifyItemRemoved(position)
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}