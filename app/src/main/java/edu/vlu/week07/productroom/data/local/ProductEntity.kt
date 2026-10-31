package edu.vlu.week07.productroom.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "product_name") val name: String,
    @ColumnInfo(name = "product_price") val price: Double,
    @ColumnInfo(name = "stock_quantity") val quantity: Int,
    // Extra column used by the star button of the Lab 07 UI (not part of the Lab 07A spec)
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)
