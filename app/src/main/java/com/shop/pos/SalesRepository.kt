package com.shop.pos

class SalesRepository(private val salesDao: SalesDao) {

    suspend fun addSaleRecord(record: SaleRecord) {
        salesDao.insert(record)
    }

    suspend fun getSaleRecords(): List<SaleRecord> {
        return salesDao.getAllSales()
    }

    suspend fun updateSaleRecord(record: SaleRecord) {
        salesDao.update(record)
    }

    suspend fun deleteSaleRecord(record: SaleRecord) {
        salesDao.delete(record)
    }

    suspend fun getSaleById(id: Int): SaleRecord? {
        return salesDao.getSaleById(id)
    }

    suspend fun getTotalSales(): Double? {
        return salesDao.getTotalSales()
    }

    /**
     * Calculates the total cost of all goods that have been sold.
     * This is essential for accurate profit calculation.
     */
    suspend fun getTotalCostOfGoodsSold(): Double? {
        var totalCost = 0.0
        salesDao.getAllSales().forEach { saleRecord ->
            // Check if the items list is not null before iterating
            saleRecord.items?.forEach { saleItem ->
                totalCost += saleItem.quantity * saleItem.costPrice
            }
        }
        return totalCost
    }

    suspend fun getSalesForCustomer(customerName: String): List<SaleRecord> {
        return salesDao.getSalesForCustomer(customerName)
    }
}
