package com.shop.pos

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ExpensesFragment : Fragment() {

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

        // Adapter ကို Repository ကနေရတဲ့ data list နဲ့ တည်ဆောက်ပါ
        expensesAdapter = ExpensesAdapter(ExpensesRepository.getExpenseItems())
        expensesRecyclerView.adapter = expensesAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddExpense.setOnClickListener {
            // TODO: ကုန်ကျစရိတ်အသစ်ထည့်ရန် Screen အသစ်ကို ဖွင့်ပါမည်
            Toast.makeText(requireContext(), "Add Expense Clicked", Toast.LENGTH_SHORT).show()
        }
    }
}