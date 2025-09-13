package com.shop.pos

object SalesRepository {
    private val saleRecords = mutableListOf<SaleRecord>()

    fun addSaleRecord(record: SaleRecord) {
        saleRecords.add(0, record)
    }

    fun getSaleRecords(): List<SaleRecord> {
        return saleRecords
    }

    // စုစုပေါင်း ရောင်းရငွေကို တွက်ချက်ရန်
    fun getTotalSales(): Double {
        return saleRecords.sumOf { it.totalAmount }
    }
}