package co.publist.core.common.data.local.db

import android.content.Context
import androidx.paging.DataSource
import co.publist.core.common.data.models.CategoryDbEntity
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
        publistDao.insert(categoriesList)
    }

    override fun getCategories(): DataSource.Factory<Int, CategoryDbEntity> {
        return publistDao.getCategories()
    }

    override fun deleteCategories() {
        return publistDao.deleteCategories()
    }

}

interface DataBaseInterface {
    fun getCategories(): DataSource.Factory<Int, CategoryDbEntity>
    fun updateCategories(categoriesList: List<CategoryDbEntity>)
    fun deleteCategories()

}