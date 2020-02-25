package co.publist.core.common.data.models

import co.publist.core.common.data.models.category.Category
import co.publist.core.common.data.models.category.CategoryDbEntity
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object Mapper {
    private fun mapToCategoryDbEntity(item: Category): CategoryDbEntity =
        CategoryDbEntity(
            id = item.id!!,
            name = item.name!!
        )

    private fun mapToCategory(categoryDbEntity: CategoryDbEntity): Category {
        return Category(
            id = categoryDbEntity.id,
            name = categoryDbEntity.name
        )
    }

    private fun mapToCategory(queryDocumentSnapshot: QueryDocumentSnapshot): Category {
        val category = queryDocumentSnapshot.toObject(Category::class.java)
        category.id = queryDocumentSnapshot.id
        return category
    }

    fun mapToCategoryDbEntityList(list: ArrayList<Category>): List<CategoryDbEntity> {
        return list.map { mapToCategoryDbEntity(it) }
    }

    fun mapToCategoryArrayList(list: List<CategoryDbEntity>): ArrayList<Category> {
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

}