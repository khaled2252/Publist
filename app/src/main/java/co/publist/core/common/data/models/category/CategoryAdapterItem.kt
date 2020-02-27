package co.publist.core.common.data.models.category

data class CategoryAdapterItem (
    var id :String? = null,
    var localizations : Localization? = null,
    var name :String? = null,
    var isSelected : Boolean = false
)