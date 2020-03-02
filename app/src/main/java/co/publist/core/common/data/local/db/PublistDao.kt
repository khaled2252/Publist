package co.publist.core.common.data.local.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.common.data.models.wish.ListDbEntity
import co.publist.core.common.data.models.wish.MyFavoritesDbEntity
import io.reactivex.Single


@Dao
interface PublistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<CategoryDbEntity>)

    @Query("SELECT * FROM Categories")
    fun getCategories(): Single<List<CategoryDbEntity>>

    @Query("SELECT * FROM Categories")
    fun getCategoriesDataSource(): DataSource.Factory<Int, CategoryDbEntity>

    @Query("DELETE FROM Categories")
    fun deleteCategories()

    @Query("SELECT * FROM lists")
    fun getLists(): Single<List<ListDbEntity>>

    @Query("SELECT * FROM lists")
    fun getListsDataSource(): DataSource.Factory<Int, ListDbEntity>

    @Query("DELETE FROM lists")
    fun deleteLists()

     @Query("SELECT * FROM myfavorites")
    fun getMyFavorites(): Single<List<MyFavoritesDbEntity>>

    @Query("SELECT * FROM myfavorites")
    fun getMyFavoritesDataSource(): DataSource.Factory<Int, MyFavoritesDbEntity>

    @Query("DELETE FROM myfavorites")
    fun deleteMyFavorites()


}