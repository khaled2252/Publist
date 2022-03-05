package com.publist.core.common.data.local.db

import android.content.Context
import com.publist.core.common.data.local.db.publist.PublistDao
import com.publist.core.common.data.local.db.publist.PublistDataBase
import com.publist.core.common.data.local.db.seenwishes.SeenWishesDao
import com.publist.core.common.data.local.db.seenwishes.SeenWishesDataBase
import com.publist.core.common.data.models.category.CategoryDbEntity
import com.publist.core.common.data.models.wish.MyListDbEntity
import com.publist.core.common.data.models.wish.SeenWish
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

    override fun updateSelectedCategories(categoriesList: List<CategoryDbEntity>) {
        publistDao.deleteCategories()
        publistDao.insertCategoriesList(categoriesList)
    }

    override fun getSelectedCategories(): Single<List<CategoryDbEntity>> {
        return publistDao.getCategories()
    }

    override fun deleteSelectedCategories() {
        return publistDao.deleteCategories()
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

    //Unused
    override fun getMyLists(): Single<List<MyListDbEntity>> {
        return publistDao.getMyLists()
    }

    override fun insertIntoMyLists(myList: MyListDbEntity) {
        return publistDao.insertIntoMyLists(myList)
    }

    override fun deleteFromMyLists(wishId: String) {
        return publistDao.deleteFromMyLists(wishId)
    }

    override fun addMyLists(myList: List<MyListDbEntity>) {
        return publistDao.insertMyLists(myList)
    }

}

interface DataBaseInterface {
    fun getSelectedCategories(): Single<List<CategoryDbEntity>>
    fun updateSelectedCategories(categoriesList: List<CategoryDbEntity>)
    fun deleteSelectedCategories()
    fun isWishSeen(wishId: String): Single<Boolean>
    fun insertSeenWish(wishId: String)

    //Unused
    fun getMyLists(): Single<List<MyListDbEntity>>
    fun insertIntoMyLists(myList: MyListDbEntity)
    fun deleteFromMyLists(wishId: String)
    fun addMyLists(myList: List<MyListDbEntity>)
}