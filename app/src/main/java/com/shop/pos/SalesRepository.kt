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
    suspend fun deleteSaleRecord(record: SaleRecord) {
        salesDao.delete(record)
    }
    suspend fun updateSaleRecord(record: SaleRecord) {
        salesDao.update(record)
    }

}