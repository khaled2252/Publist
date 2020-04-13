package co.publist.core.common.data.local.db.seenwishes

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import co.publist.core.common.data.models.wish.SeenWish
import io.reactivex.Single


@Dao
interface SeenWishesDao {
    @Insert()
    fun insertSeenWish(wish: SeenWish)

    @Query("SELECT COUNT(*) FROM SeenWishes WHERE wish_id= :wishId")
    fun isSeenWishExist(wishId: String): Single<Int>

}