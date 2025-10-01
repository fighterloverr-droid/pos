package com.shop.pos

interface SalesItemListener {
    fun onDeleteItem(position: Int)
    fun onIncreaseQuantity(position: Int)
    fun onDecreaseQuantity(position: Int)
}