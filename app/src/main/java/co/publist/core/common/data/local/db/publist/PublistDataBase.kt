package co.publist.core.common.data.local.db.publist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.publist.core.common.data.local.db.Converters
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.common.data.models.wish.MyFavoritesDbEntity
import co.publist.core.common.data.models.wish.MyListDbEntity
import co.publist.core.utils.Utils.Constants.DB_NAME


@Database(
    entities = [CategoryDbEntity::class, MyListDbEntity::class, MyFavoritesDbEntity::class],
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
            INSTANCE
                ?: synchronized(this) {
                    INSTANCE
                        ?: buildDatabase(
                            context
                        )
                            .also { INSTANCE = it }
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