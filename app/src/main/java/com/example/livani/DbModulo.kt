package com.example.livani

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): ProductDatabase {
        return ProductDatabase.getDatabase(app)
    }

    @Provides
    fun provideProductDao(database: ProductDatabase): ProductDao {
        return database.productDao()
    }
}