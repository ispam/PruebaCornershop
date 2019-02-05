package com.pruebacornershop.Activities

import android.app.Application
import com.pruebacornershop.DI.AppComponent
import com.pruebacornershop.DI.AppModule
import com.pruebacornershop.DI.DaggerAppComponent

class App: Application() {

    companion object {
        @JvmStatic
        lateinit var component: AppComponent
    }

    override fun onCreate() {
        super.onCreate()

        component = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

    }

}