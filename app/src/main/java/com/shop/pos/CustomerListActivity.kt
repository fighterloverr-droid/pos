package com.shop.pos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.launch

class CustomerListActivity : AppCompatActivity() {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var customerAdapter: CustomerListAdapter
    private val customerList = mutableListOf<Customer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_list)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val app = application as PosApplication
        customerRepository = CustomerRepository(app.database.customerDao())

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        loadCustomers()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewCustomers)
        customerAdapter = CustomerListAdapter(customerList) { customer ->
            // Click listener အသစ်
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("EXTRA_CUSTOMER", customer)
            startActivity(intent)
        }
        recyclerView.adapter = customerAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadCustomers() {
        lifecycleScope.launch {
            val customersFromDb = customerRepository.getAllCustomers()
            customerList.clear()
            customerList.addAll(customersFromDb)
            runOnUiThread {
                customerAdapter.updateList(customerList)
            }
        }
    }
}
    
