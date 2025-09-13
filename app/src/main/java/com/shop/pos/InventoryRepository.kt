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

    // Function အသစ်: စုစုပေါင်းပစ္စည်းတန်ဖိုးကို တွက်ချက်ရန်
    fun getTotalInventoryValue(): Double {
        var totalValue = 0.0
        inventoryItems.forEach { item ->
            totalValue += item.stockQuantity * item.price
        }
        return totalValue
    }
}