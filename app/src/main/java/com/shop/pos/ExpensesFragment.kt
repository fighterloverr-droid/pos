package com.shop.pos

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ExpensesFragment : Fragment(), ExpenseItemListener {

    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var fabAddExpense: FloatingActionButton
    private lateinit var expensesRepository: ExpensesRepository
    private var expenseItems = mutableListOf<ExpenseItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = (requireActivity().application as PosApplication).database.expensesDao()
        expensesRepository = ExpensesRepository(dao)

        expensesRecyclerView = view.findViewById(R.id.recyclerViewExpenses)
        fabAddExpense = view.findViewById(R.id.fabAddExpense)

        expensesAdapter = ExpensesAdapter(expenseItems, this)
        expensesRecyclerView.adapter = expensesAdapter
        expensesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddExpense.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadExpenses()
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            val itemsFromDb = expensesRepository.getExpenseItems()
            expenseItems.clear()
            expenseItems.addAll(itemsFromDb)
            expensesAdapter.notifyDataSetChanged()
        }
    }

    override fun onEditItem(position: Int) {
        val itemToEdit = expenseItems[position]
        val intent = Intent(requireContext(), AddExpenseActivity::class.java)
        intent.putExtra("EDIT_EXPENSE_ID", itemToEdit.id)
        startActivity(intent)
    }

    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("မှတ်တမ်း ဖျက်ရန်")
            .setMessage("ဒီကုန်ကျစရိတ်မှတ်တမ်းကို ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                lifecycleScope.launch {
                    val itemToDelete = expenseItems[position]
                    expensesRepository.deleteExpenseItem(itemToDelete)
                    loadExpenses()
                }
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}