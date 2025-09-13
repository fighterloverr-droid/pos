package com.shop.pos

object InventoryRepository {
    private val inventoryItems = mutableListOf<InventoryItem>()

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        if (inventoryItems.isEmpty()) {
            inventoryItems.add(InventoryItem("Coca-Cola", 50, 1000.0))
            inventoryItems.add(InventoryItem("Potato Chips", 30, 1500.0))
            inventoryItems.add(InventoryItem("Chocolate Bar", 100, 800.0))
            inventoryItems.add(InventoryItem("Mineral Water", 80, 500.0))
        }
    }

    fun getInventoryItems(): List<InventoryItem> {
        return inventoryItems
    }

    fun addInventoryItem(item: InventoryItem) {
        inventoryItems.add(item)
    }

    fun updateInventoryItem(position: Int, updatedItem: InventoryItem) {
        if (position >= 0 && position < inventoryItems.size) {
            inventoryItems[position] = updatedItem
        }
    }

    fun deleteInventoryItem(position: Int) {
        if (position >= 0 && position < inventoryItems.size) {
            inventoryItems.removeAt(position)
        }
    }

    fun addStockFromPurchase(purchaseDetailItems: List<PurchaseDetailItem>) {
        purchaseDetailItems.forEach { purchasedItem ->
            val existingItem = inventoryItems.find { it.name.equals(purchasedItem.name, ignoreCase = true) }

            if (existingItem != null) {
                val index = inventoryItems.indexOf(existingItem)
                // အမှားပြင်ဆင်ပြီး : 'existing' အစား 'existingItem' ကို အသုံးပြုပါ
                val updatedQuantity = existingItem.stockQuantity + purchasedItem.quantity
                inventoryItems[index] = existingItem.copy(stockQuantity = updatedQuantity)
            } else {
                val newItem = InventoryItem(
                    name = purchasedItem.name,
                    stockQuantity = purchasedItem.quantity,
                    price = purchasedItem.purchasePrice
                )
                inventoryItems.add(newItem)
            }
        }
    }

    fun deductStockFromSale(saleItems: List<SaleItem>): Boolean {
        for (saleItem in saleItems) {
            val inventoryItem = inventoryItems.find { it.name.equals(saleItem.name, ignoreCase = true) }
            if (inventoryItem == null || inventoryItem.stockQuantity < saleItem.quantity) {
                return false
            }
        }

        for (saleItem in saleItems) {
            val inventoryItem = inventoryItems.find { it.name.equals(saleItem.name, ignoreCase = true) }!!
            val index = inventoryItems.indexOf(inventoryItem)
            val updatedQuantity = inventoryItem.stockQuantity - saleItem.quantity
            inventoryItems[index] = inventoryItem.copy(stockQuantity = updatedQuantity)
        }

        return true
    }

    fun getTotalInventoryValue(): Double {
        var totalValue = 0.0
        inventoryItems.forEach { item ->
            totalValue += item.stockQuantity * item.price
        }
        return totalValue
    }
}