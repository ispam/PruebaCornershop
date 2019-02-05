package com.pruebacornershop.Data.Local.ViewModels

import com.pruebacornershop.Data.Local.Entities.Total
import com.pruebacornershop.Data.Local.PruebaCornershopDB
import io.reactivex.Completable
import javax.inject.Inject

class TotalViewModel @Inject constructor(private val db: PruebaCornershopDB) {

    fun insertTotal(total: Total): Completable = Completable.fromAction { db.totalDAO().createTotal(total) }

    fun getTotal() =  db.totalDAO().getTotal()

    fun updateTotal(total: Long, totalID: Long) = Completable.fromCallable { db.totalDAO().updateTotal(total, totalID) }
}