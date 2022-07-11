package com.publist.core.common.data.models.category

data class CategoryAdapterItem(
    var id: String? = null,
    var localizations: Localization? = null,
    var name: String? = null,
    var isSelected: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        return this.id == (other as CategoryAdapterItem).id
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}