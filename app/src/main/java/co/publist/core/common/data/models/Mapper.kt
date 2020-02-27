package co.publist.core.common.data.models

import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryAdapterItem
import co.publist.core.common.data.models.category.CategoryDbEntity
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

    fun mapToCategory(item: CategoryAdapterItem): Category {
        return Category(
            id = item.id,
            localizations = item.localizations,
            name = item.name
        )
    }

    fun mapToCategoryAdapterItem(category: Category): CategoryAdapterItem {
        return CategoryAdapterItem(
            id = category.id,
            localizations = category.localizations,
            name = category.name
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

    fun mapToCategoryAdapterItemList(list:  ArrayList<Category>): ArrayList<CategoryAdapterItem> {
        val arrayList = ArrayList<CategoryAdapterItem>()
        for (item in list)
            arrayList.add(mapToCategoryAdapterItem(item))
        return arrayList
    }

}