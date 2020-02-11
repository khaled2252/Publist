package co.publist.core.common.data.local.db

import android.content.Context
import co.publist.core.common.data.models.CategoryDbEntity
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Inject

class DataBaseAccess @Inject constructor(context: Context) : DataBaseInterface {
    private val database: PublistDataBase
    private val publistDao: PublistDao
    private val ioExecutor: Executor

    init {
        database = PublistDataBase.getInstance(context)
        publistDao = database.taniaDao()
        ioExecutor = Executors.newSingleThreadExecutor()
    }

    override fun insert(categoriesList: List<CategoryDbEntity>) {
        publistDao.insert(categoriesList)
    }


//    override fun getCouponById(id: Int): CouponDbEntity? {
//        return publistDao.getCouponById(id)
//    }
//
//    override fun getItems(): DataSource.Factory<Int, ItemDbEntity> {
//        return publistDao.getItems()
//    }
//
//    override fun deleteAllItems() {
//        return publistDao.deleteAllItems()
//    }
//
//    override fun updateItem(id: Int, count: Int) {
//        ioExecutor.execute { publistDao.updateItem(id, count) }
//    }

}
interface DataBaseInterface {
    fun insert(categoriesList: List<CategoryDbEntity>)
//    fun getCouponById(id: Int): CouponDbEntity?
//    fun getItems(): DataSource.Factory<Int, ItemDbEntity>
//    fun deleteAllItems()
//    fun updateItem(id: Int, count: Int)

}