package com.publist.core.common.data.local.db.publist

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.publist.core.common.data.models.category.CategoryDbEntity
import com.publist.core.common.data.models.wish.MyFavoritesDbEntity
import com.publist.core.common.data.models.wish.MyListDbEntity
import io.reactivex.Single


@Dao
interface PublistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCategoriesList(items: List<CategoryDbEntity>)

    @Query("SELECT * FROM Categories")
    fun getCategories(): Single<List<CategoryDbEntity>>

    @Query("SELECT * FROM Categories")
    fun getCategoriesDataSource(): DataSource.Factory<Int, CategoryDbEntity>

    @Query("DELETE FROM Categories")
    fun deleteCategories()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMyLists(items: List<MyListDbEntity>)

    @Insert
    fun insertIntoMyLists(item: MyListDbEntity)

    @Query("SELECT * FROM myLists")
    fun getMyLists(): Single<List<MyListDbEntity>>

    @Query("SELECT * FROM myLists")
    fun getMyListsDataSource(): DataSource.Factory<Int, MyListDbEntity>

    @Query("DELETE FROM myLists")
    fun deleteMyLists()

    @Query("DELETE FROM myLists WHERE wish_id = :wishId")
    fun deleteFromMyLists(wishId: String)


    @Query("SELECT * FROM myFavorites")
    fun getMyFavorites(): Single<List<MyFavoritesDbEntity>>

    @Query("SELECT * FROM myFavorites")
    fun getMyFavoritesDataSource(): DataSource.Factory<Int, MyFavoritesDbEntity>

    @Query("DELETE FROM myFavorites")
    fun deleteMyFavorites()


}