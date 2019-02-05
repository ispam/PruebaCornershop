package com.pruebacornershop.Data.Local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pruebacornershop.Data.Local.DAOs.TotalDAO
import com.pruebacornershop.Data.Local.Entities.Total

@Database(entities = arrayOf(Total::class), version = 1, exportSchema = false)
abstract class PruebaCornershopDB: RoomDatabase() {

    abstract fun totalDAO(): TotalDAO
}