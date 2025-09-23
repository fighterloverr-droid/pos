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

    suspend fun getTotalCostOfGoodsSold(): Double {
        var totalCost = 0.0
        salesDao.getAllSales().forEach { saleRecord ->
            saleRecord.items.forEach { saleItem ->
                totalCost += saleItem.quantity * saleItem.costPrice
            }
        }
        return totalCost
    }
    suspend fun getSaleById(id: Int): SaleRecord? {
        return salesDao.getSaleById(id)
    }
}
