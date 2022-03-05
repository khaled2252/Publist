package com.publist.core.common.data.local.db.seenwishes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.publist.core.common.data.models.wish.SeenWish


@Database(
    entities = [SeenWish::class],
    version = 1,
    exportSchema = false
)

abstract class SeenWishesDataBase : RoomDatabase() {
    abstract fun seenWishesDao(): SeenWishesDao

    companion object {

        @Volatile
        private var INSTANCE: SeenWishesDataBase? = null

        fun getInstance(context: Context): SeenWishesDataBase =
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: buildDatabase(
                            context
                        )
                            .also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
            Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                SeenWishesDataBase::class.java
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}