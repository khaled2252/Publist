package com.publist.core.common.data.models

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.publist.core.common.data.models.category.Category
import com.publist.core.common.data.models.category.CategoryAdapterItem
import com.publist.core.common.data.models.category.CategoryDbEntity
import com.publist.core.common.data.models.category.Localization
import com.publist.core.common.data.models.wish.*
import com.publist.core.utils.Utils.Constants.ALGOLIA_HITS_FIELD
import com.publist.core.utils.Utils.Constants.ALGOLIA_WISH_ID_FIELD
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


object Mapper {
    private fun mapToCategoryDbEntity(item: Category): CategoryDbEntity =
        CategoryDbEntity(
            id = item.id!!,
            localizations = item.localizations!!,
            name = item.name!!
        )

    private fun mapToCategoryAdapterItem(categoryDbEntity: CategoryDbEntity): CategoryAdapterItem {
        return CategoryAdapterItem(
            id = categoryDbEntity.id,
            localizations = categoryDbEntity.localizations,
            name = categoryDbEntity.name,
            isSelected = true
        )
    }

    private fun mapToCategory(queryDocumentSnapshot: QueryDocumentSnapshot): Category {
        val category = queryDocumentSnapshot.toObject(Category::class.java)
        category.id = queryDocumentSnapshot.id
        return category
    }

    private fun mapToWish(queryDocumentSnapshot: QueryDocumentSnapshot): Wish {
        val wish = queryDocumentSnapshot.toObject(Wish::class.java)
        wish.wishId = queryDocumentSnapshot.id
        return wish
    }

    private fun mapToWish(item: MyListDbEntity): Wish {
        return Wish(
            wishId = item.wish_id,
            category = item.category,
            categoryId = item.category_id,
            creator = item.creator,
            date = item.date,
            favoritesCount = item.favorites_count,
            items = item.items,
            itemsId = item.items_id,
            title = item.title,
            wishPhotoURL = item.wish_photo_url,
            photoName = item.photo_name
        )
    }

    fun mapToCategory(item: CategoryAdapterItem): Category {
        return Category(
            id = item.id,
            localizations = item.localizations,
            name = item.name
        )
    }

    fun mapToCategoryAdapterItem(item: Category): CategoryAdapterItem {
        return CategoryAdapterItem(
            id = item.id,
            localizations = item.localizations,
            name = item.name
        )
    }

    fun mapToCategoryAdapterItem(item: CategoryWish): CategoryAdapterItem {
        val currentDeviceLanguage = Locale.getDefault().language
        val localization = Localization(null, null)
        if (currentDeviceLanguage == "en")
            localization.en = item.name
        else
            localization.ar = item.name
        return CategoryAdapterItem(
            id = item.id,
            localizations = localization,
            name = item.name
        )
    }

    fun mapToCategoryDbEntityList(list: ArrayList<Category>): List<CategoryDbEntity> {
        return list.map { mapToCategoryDbEntity(it) }
    }

    fun mapToCategoryArrayList(list: List<CategoryAdapterItem>): ArrayList<Category> {
        val arrayList = ArrayList<Category>()
        for (item in list)
            arrayList.add(mapToCategory(item))
        return arrayList
    }

    fun mapToCategoryArrayList(documents: QuerySnapshot): ArrayList<Category> {
        val arrayList = ArrayList<Category>()
        for (document in documents)
            arrayList.add(mapToCategory(document))
        return arrayList
    }

    fun mapToCategoryAdapterItemList(list: List<CategoryDbEntity>): ArrayList<CategoryAdapterItem> {
        val arrayList = ArrayList<CategoryAdapterItem>()
        for (item in list)
            arrayList.add(mapToCategoryAdapterItem(item))
        return arrayList
    }

    fun mapToCategoryAdapterItemList(list: ArrayList<Category>): ArrayList<CategoryAdapterItem> {
        val arrayList = ArrayList<CategoryAdapterItem>()
        for (item in list)
            arrayList.add(mapToCategoryAdapterItem(item))
        return arrayList
    }

    fun mapToWishAdapterItemArrayList(documents: QuerySnapshot): ArrayList<Wish> {
        val arrayList = ArrayList<Wish>()
        for (document in documents)
            arrayList.add(mapToWish(document))
        return arrayList
    }

    fun mapToWishAdapterItemArrayList(list: List<MyListDbEntity>): ArrayList<Wish> {
        val arrayList = ArrayList<Wish>()
        for (item in list)
            arrayList.add(mapToWish(item))
        return arrayList
    }

    fun mapToListDbEntity(item: Wish): MyListDbEntity {
        return MyListDbEntity(
            wish_id = item.wishId!!,
            category = item.category!!,
            category_id = item.categoryId!!,
            creator = item.creator!!,
            date = item.date!!,
            favorites_count = item.favoritesCount,
            items = item.items!!,
            items_id = item.itemsId!!,
            title = item.title!!,
            wish_photo_url = item.wishPhotoURL!!,
            photo_name = item.photoName!!
        )
    }

    fun mapToMyListDbEntityList(list: ArrayList<Wish>): List<MyListDbEntity> {
        return list.map { mapToListDbEntity(it) }
    }

    fun mapToWishAdapterItemArrayList(list: ArrayList<Wish>): ArrayList<WishAdapterItem> {
        val arrayList = ArrayList<WishAdapterItem>()
        for (item in list)
            arrayList.add(mapToWishAdapterItem(item))
        return arrayList
    }

    fun mapToWishArrayList(documents: QuerySnapshot): ArrayList<Wish> {
        var arrayList = ArrayList<Wish>()
        for (document in documents)
            arrayList.add(mapToWish(document))
        arrayList.sortByDescending { it.date }
        return arrayList
    }

    fun mapToWishIdsArrayList(jsonObject: JSONObject): ArrayList<String> {
        val hitsJsonArray = jsonObject.getJSONArray(ALGOLIA_HITS_FIELD)
        val wishIdsArrayList = arrayListOf<String>()
        for (wishHitIndex in 0 until hitsJsonArray.length()) {
            val wishHitJsonObject = hitsJsonArray.getJSONObject(wishHitIndex)
            wishIdsArrayList.add(wishHitJsonObject.getString(ALGOLIA_WISH_ID_FIELD))
        }
        return wishIdsArrayList
    }

    fun mapToWishAdapterItem(item: Wish): WishAdapterItem {
        return WishAdapterItem(
            category = item.category,
            categoryId = item.categoryId,
            creator = item.creator,
            date = item.date,
            items = item.items,
            itemsId = item.itemsId,
            title = item.title,
            wishId = item.wishId,
            wishPhotoURL = item.wishPhotoURL
        )
    }

    fun mapToWish(item: WishAdapterItem): Wish {
        return Wish(
            category = item.category,
            categoryId = item.categoryId,
            creator = item.creator,
            date = item.date,
            items = item.items,
            itemsId = item.itemsId,
            title = item.title,
            wishId = item.wishId,
            wishPhotoURL = item.wishPhotoURL,
            photoName = item.photoName,
            favoritesCount = item.favoritesCount,
            organicSeenCount = item.organicSeenCount,
            seenCount = item.seenCount
        )
    }

    fun mapToCategoryWish(item: Category): CategoryWish {
        return CategoryWish(
            id = item.id,
            lang = Locale.getDefault().language,
            name = item.name
        )
    }

    fun mapToEditedWishItem(wish: Wish): EditedWish {
        return EditedWish(
            wish.category,
            wish.categoryId,
            wish.date,
            wish.title,
            wish.creator,
            wish.favoritesCount,
            wish.wishPhotoURL,
            wish.photoName,
            mapToEditedWishItems(wish.items!!),
            wish.itemsId,
            wish.wishId,
            wish.seenCount,
            wish.organicSeenCount
        )
    }

    private fun mapToEditedWishItems(items: Map<String, WishItem>): Map<String, EditedWishItem> {
        val resultItems = mutableMapOf<String, EditedWishItem>()
        for (item in items)
            resultItems[item.key] = mapToEditedWishItem(item.value)
        return resultItems
    }

    private fun mapToEditedWishItem(item: WishItem): EditedWishItem {
        return EditedWishItem(
            item.name,
            item.orderId,
            item.completeCount,
            item.viewedCount,
            item.topCompletedUsersId,
            item.topViewedUsersId,
            item.done,
            item.isLiked
        )
    }

}