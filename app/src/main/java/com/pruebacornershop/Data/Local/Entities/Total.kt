package com.pruebacornershop.Data.Local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Total(
    var total: Long
) {
    @PrimaryKey(autoGenerate = true)
    var total_id: Long = 0
}