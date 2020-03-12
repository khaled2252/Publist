package co.publist.core.common.data.models

import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.category.CategoryDbEntity
import co.publist.core.common.data.models.wish.CategoryWish
import co.publist.core.common.data.models.wish.MyListDbEntity
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.common.data.models.wish.WishAdapterItem
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

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
            wishPhotoURL = item.wish_photo_url
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
            wish_photo_url = item.wishPhotoURL!!
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

     fun mapToWishAdapterItem(item: Wish): WishAdapterItem {
        return WishAdapterItem(
            category = item.category,
            categoryId = item.categoryId,
            creator = item.creator,
            date = item.date,
            favoritesCount = item.favoritesCount,
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
            favoritesCount = item.favoritesCount,
            items = item.items,
            itemsId = item.itemsId,
            title = item.title,
            wishId = item.wishId,
            wishPhotoURL = item.wishPhotoURL
        )
    }

    fun mapToCategoryWish(item : Category) : CategoryWish {
        return CategoryWish(
            id = item.id,
            lang = "en",
            name = item.name
        )
    }
}