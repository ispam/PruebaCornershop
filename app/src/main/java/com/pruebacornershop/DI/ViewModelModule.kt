package com.pruebacornershop.DI

import android.content.Context
import androidx.room.Room
import com.pruebacornershop.Data.Local.PruebaCornershopDB
import com.pruebacornershop.Data.Local.ViewModels.TotalViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class ViewModelModule {

    companion object {
        private const val DB_NAME = "prueba_cornershop.db"
    }

    @Singleton
    @Provides
    fun provideDB(context: Context): PruebaCornershopDB =
        Room.databaseBuilder(
            context.applicationContext,
            PruebaCornershopDB::class.java,
            DB_NAME
            ).build()

    @Singleton @Provides
    fun provideTotalVM(db: PruebaCornershopDB) = TotalViewModel(db)
}