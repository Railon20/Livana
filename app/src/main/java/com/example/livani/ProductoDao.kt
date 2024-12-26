package com.example.livani

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource


@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice AND brand LIKE :brand AND type LIKE :type")
    fun searchProducts(minPrice: Double, maxPrice: Double, brand: String, type: String): PagingSource<Int, Product>

    @Insert
    suspend fun insertProducts(vararg products: Product)
}