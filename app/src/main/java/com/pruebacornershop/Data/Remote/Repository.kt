package com.pruebacornershop.Data.Remote

import android.content.Context
import com.pruebacornershop.Utils.isOnline

fun repository(function1: () -> Unit, function2: () -> Unit, context: Context) {
    if (isOnline(context)) {
        function1.invoke()
    } else {
        function2.invoke()
    }
}