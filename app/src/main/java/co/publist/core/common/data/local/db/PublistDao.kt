package co.publist.core.common.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import co.publist.core.common.data.models.CategoryDbEntity


@Dao
interface PublistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(items: List<CategoryDbEntity>)

//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    fun insertCoupons(items: List<CouponDbEntity>)
//
//    @Insert
//    fun insertCoupon(couponDb: CouponDbEntity)
//
//    @Query("SELECT * FROM Coupons ")
//    fun getCoupons(): DataSource.Factory<Int, CouponDbEntity>
//
//    @Query("DELETE FROM Coupons")
//    fun deleteAllCoupons()
//
//    @Query("UPDATE Coupons SET count = :count WHERE id = :id")
//    fun updateCoupon(id: Int, count: Int)
//
//    @Query("SELECT * FROM Coupons WHERE id LIKE :id")
//    fun getCouponById(id: Int): CouponDbEntity
//
//    @Query("UPDATE Coupons SET count= :count,price= :price , balance= :balance , discountPercentage =:discount_percentage ,description =:description,name =:name WHERE id = :id")
//    fun updateCouponEntity(
//        id: Int,
//        count: Int,
//        price: Double,
//        balance: Double,
//        discount_percentage: Double,
//        description: String,
//        name: String
//    )

//    @Query("DELETE FROM Items")
//    fun deleteAllItems()
//
//    @Query("SELECT * FROM Items ")
//    fun getItems(): DataSource.Factory<Int, ItemDbEntity>
//
//    @Query("SELECT * FROM Items WHERE id LIKE :id")
//    fun getItem(id: Int): Single<ItemDbEntity>
//
//    @Query("UPDATE Items SET count = :count WHERE id = :id")
//    fun updateItem(id: Int, count: Int)
//
//    @Query("select (SELECT COUNT(count) FROM Coupons WHERE count > 0)+(SELECT COUNT(count) FROM Items WHERE count > 0)")
//    fun getCartCount(): Single<Int>
//
//    //get cart items
//    @Query("SELECT * FROM Items WHERE count>0")
//    fun getCartItems(): DataSource.Factory<Int, ItemDbEntity>
//
//    //calculate receipt and checkout
//    @Query("SELECT * FROM Items WHERE count>0")
//    fun getAddedItems(): Single<List<ItemDbEntity>>


}