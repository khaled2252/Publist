package co.publist.core.common.data.local.db

import android.content.Context
import androidx.paging.DataSource
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.common.data.models.wish.MyListDbEntity
import io.reactivex.Single
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class DataBaseAccess @Inject constructor(context: Context) : DataBaseInterface {
    private val database: PublistDataBase = PublistDataBase.getInstance(context)
    private val publistDao: PublistDao
    private val ioExecutor: Executor

    init {
        publistDao = database.publistDao()
        ioExecutor = Executors.newSingleThreadExecutor()
    }

    override fun updateCategories(categoriesList: List<CategoryDbEntity>) {
        publistDao.deleteCategories()
        publistDao.insertCategoriesList(categoriesList)
    }

    override fun getCategories(): Single<List<CategoryDbEntity>>
    {
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
        return publistDao.insertIntoMylists(myList)
    }

    override fun deleteFromMyLists(wishId : String) {
        return publistDao.deleteFromMyLists(wishId)
    }

}

interface DataBaseInterface {
    fun getCategories():Single<List<CategoryDbEntity>>
    fun getCategoriesDataSource(): DataSource.Factory<Int, CategoryDbEntity>
    fun updateCategories(categoriesList: List<CategoryDbEntity>)
    fun deleteCategories()
    fun getMyLists():Single<List<MyListDbEntity>>
    fun getMyListsDataSource(): DataSource.Factory<Int, MyListDbEntity>
    fun insertIntoMyLists(myList: MyListDbEntity)
    fun deleteFromMyLists(wishId : String)

}