package co.publist.core.common.data.local.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.publist.core.common.data.models.CategoryDbEntity


@Dao
interface PublistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<CategoryDbEntity>)

    @Query("SELECT * FROM Categories ")
    fun getCategories(): DataSource.Factory<Int, CategoryDbEntity>

    @Query("DELETE FROM Categories")
        fun deleteCategories()
}