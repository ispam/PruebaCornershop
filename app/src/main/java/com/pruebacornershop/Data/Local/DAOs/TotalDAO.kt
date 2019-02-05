package com.pruebacornershop.Data.Local.DAOs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pruebacornershop.Data.Local.Entities.Total
import io.reactivex.Flowable

@Dao
interface TotalDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun createTotal(total: Total)

    @Query("select total from total")
    fun getTotal(): Flowable<Long>

    @Query("update total set total = :total where total_id = :totalID")
    fun updateTotal(total: Long, totalID: Long)
}