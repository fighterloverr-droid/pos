package com.shop.pos

class CustomerRepository(private val customerDao: CustomerDao) {

    suspend fun getAllCustomers(): List<Customer> {
        return customerDao.getAllCustomers()
    }

    suspend fun insertCustomer(customer: Customer) {
        customerDao.insert(customer)
    }
}