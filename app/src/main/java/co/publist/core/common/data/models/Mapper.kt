package co.publist.core.common.data.models

import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot

object Mapper {
     private fun mapToCategoryDbEntity(item: String): CategoryDbEntity =
        CategoryDbEntity(
            id = item
        )

     private fun mapToString(categoryDbEntity: CategoryDbEntity): String =
        categoryDbEntity.id

     private fun mapToString(queryDocumentSnapshot: QueryDocumentSnapshot): String =
        queryDocumentSnapshot.id

    fun mapToCategoryDbEntityList(list : ArrayList<String>): List<CategoryDbEntity> {
        return list.map { mapToCategoryDbEntity(it) }
    }

    fun mapToStringArrayList(list: List<CategoryDbEntity>): ArrayList<String> {
        val arrayList = ArrayList<String>()
        for(item in list)
            arrayList.add(mapToString(item))
        return arrayList
    }

    fun mapToStringArrayList(documents: QuerySnapshot): ArrayList<String> {
        val arrayList = ArrayList<String>()
        for(document in documents)
            arrayList.add(mapToString(document))
        return arrayList
    }

}