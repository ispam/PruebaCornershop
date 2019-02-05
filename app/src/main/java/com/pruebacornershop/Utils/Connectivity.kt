package com.pruebacornershop.Utils

import android.content.Context
import android.net.ConnectivityManager
import android.widget.Toast

fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val netInfo = cm.activeNetworkInfo
    return netInfo != null && netInfo.isConnectedOrConnecting
}

fun noInternetConnection(context: Context) {
    Toast.makeText(context, "No hay conexion a internet, por favor intentelo mas tarde", Toast.LENGTH_SHORT).show()
}