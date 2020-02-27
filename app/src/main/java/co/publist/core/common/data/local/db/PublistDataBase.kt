package co.publist.core.common.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.utils.Extensions.Constants.DB_NAME


@Database(
    entities = [CategoryDbEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)

abstract class PublistDataBase : RoomDatabase() {
    abstract fun publistDao(): PublistDao

    companion object {

        @Volatile
        private var INSTANCE: PublistDataBase? = null

        fun getInstance(context: Context): PublistDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                PublistDataBase::class.java
                , DB_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
    }
}