package co.publist.core.common.data.local.db

import android.content.Context
import androidx.paging.DataSource
import co.publist.core.common.data.local.db.publist.PublistDao
import co.publist.core.common.data.local.db.publist.PublistDataBase
import co.publist.core.common.data.local.db.seenwishes.SeenWishesDao
import co.publist.core.common.data.local.db.seenwishes.SeenWishesDataBase
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.common.data.models.wish.MyListDbEntity
import co.publist.core.common.data.models.wish.SeenWish
import io.reactivex.Single
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class DataBaseAccess @Inject constructor(context: Context) : DataBaseInterface {
    private val database: PublistDataBase = PublistDataBase.getInstance(context)
    private val seenWishesDatabase: SeenWishesDataBase = SeenWishesDataBase.getInstance(context)
    private val publistDao: PublistDao
    private val seenWishesDao: SeenWishesDao
    private val ioExecutor: Executor

    init {
        publistDao = database.publistDao()
        seenWishesDao = seenWishesDatabase.seenWishesDao()
        ioExecutor = Executors.newSingleThreadExecutor()
    }

    override fun updateCategories(categoriesList: List<CategoryDbEntity>) {
        publistDao.deleteCategories()
        publistDao.insertCategoriesList(categoriesList)
    }

    override fun getCategories(): Single<List<CategoryDbEntity>> {
        return publistDao.getCategories()
    }

    override fun getCategoriesDataSource(): DataSource.Factory<Int, CategoryDbEntity> {
        return publistDao.getCategoriesDataSource()
    }

    override fun deleteCategories() {
        return publistDao.deleteCategories()
    }

    override fun getMyLists(): Single<List<MyListDbEntity>> {
        return publistDao.getMyLists()
    }

    override fun getMyListsDataSource(): DataSource.Factory<Int, MyListDbEntity> {
        return publistDao.getMyListsDataSource()
    }

    override fun insertIntoMyLists(myList: MyListDbEntity) {
        return publistDao.insertIntoMyLists(myList)
    }

    override fun addMyLists(myList: List<MyListDbEntity>) {
        return publistDao.insertMyLists(myList)
    }

    override fun deleteFromMyLists(wishId: String) {
        return publistDao.deleteFromMyLists(wishId)
    }

    override fun isWishSeen(wishId: String): Single<Boolean> {
        return seenWishesDao.isSeenWishExist(wishId).flatMap { count ->
            if (count == 0)
                Single.just(false)
            else
                Single.just(true)
        }
    }

    override fun insertSeenWish(wishId: String) {
        return seenWishesDao.insertSeenWish(SeenWish(wishId))
    }

}

interface DataBaseInterface {
    fun getCategories(): Single<List<CategoryDbEntity>>
    fun getCategoriesDataSource(): DataSource.Factory<Int, CategoryDbEntity>
    fun updateCategories(categoriesList: List<CategoryDbEntity>)
    fun deleteCategories()
    fun getMyLists(): Single<List<MyListDbEntity>>
    fun getMyListsDataSource(): DataSource.Factory<Int, MyListDbEntity>
    fun insertIntoMyLists(myList: MyListDbEntity)
    fun addMyLists(myList: List<MyListDbEntity>)
    fun deleteFromMyLists(wishId: String)
    fun isWishSeen(wishId: String): Single<Boolean>
    fun insertSeenWish(wishId: String)
}