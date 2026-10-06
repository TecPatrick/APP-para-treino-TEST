package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_receipts")
data class PurchaseReceipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String = "Supermercado / Growth Suplementos",
    val receiptNumber: String = "", // Chave de acesso / Número da NF-e
    val dateMillis: Long = System.currentTimeMillis(),
    val dateDisplay: String = "",
    val monthYearKey: String = "", // "2026-10"
    val totalAmount: Float = 0f,
    val itemCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "purchase_item_logs")
data class PurchaseItemLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptId: Long = 0,
    val foodName: String,
    val category: FoodCategory,
    val quantity: Float,
    val unit: String, // "kg", "un", "pct (1kg)"
    val unitPricePaid: Float, // Price per unit/kg (e.g. 21.90)
    val totalPricePaid: Float, // quantity * unitPricePaid
    val dateMillis: Long,
    val monthYearKey: String, // "2026-10"
    val storeName: String = ""
)

data class ItemPriceInflationStat(
    val foodName: String,
    val category: FoodCategory,
    val unit: String,
    val currentAvgPrice: Float,
    val previousAvgPrice: Float,
    val inflationPercentage: Float, // e.g. +7.5% or -3.2%
    val latestDateDisplay: String
)
