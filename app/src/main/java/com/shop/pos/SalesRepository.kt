package com.shop.pos

class SalesRepository(private val salesDao: SalesDao) {

    suspend fun addSaleRecord(record: SaleRecord) {
        salesDao.insert(record)
    }

    suspend fun getSaleRecords(): List<SaleRecord> {
        return salesDao.getAllSales()
    }

    suspend fun getTotalSales(): Double? {
        return salesDao.getTotalSales()
    }
}