package co.publist.core.utils

import android.app.Activity
import android.content.Context
import android.os.IBinder
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import co.publist.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso
import kotlin.math.abs


object Utils {

    fun hideSoftKeyboard(context: Context, iBinder: IBinder) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(iBinder, 0)
    }

    @JvmStatic
    @BindingAdapter("profilePictureUrl")
    fun loadProfilePicture(view: ImageView, imageUrl: String?) {
        Glide.with(view)
            .load(imageUrl)
            .placeholder(R.drawable.ic_guest)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }

    @JvmStatic
    @BindingAdapter("wishImageUrl")
    fun loadWishImage(view: ImageView, imageUrl: String?) {
        if (!imageUrl.isNullOrEmpty()) {
            view.visibility = View.VISIBLE
            Picasso.get()
                .load(imageUrl)
                .fit()
                .error(R.drawable.ph_wish_image)
                .placeholder(R.drawable.ph_wish_image)
                .into(view)
        }
    }

    fun getDistanceBetweenViews(
        firstView: View,
        secondView: View
    ): Int {
        val firstPosition = IntArray(2)
        val secondPosition = IntArray(2)
        firstView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        firstView.getLocationInWindow(firstPosition)
        secondView.getLocationInWindow(secondPosition)
        val b = firstView.measuredHeight + firstPosition[1]
        val t = secondPosition[1]
        return abs(b - t)
    }

    class DragManageAdapter(private val adapter: ItemsAdapter, dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs,0)
    {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean
        {
            adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int)
        {

        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)

            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
            }
        }
        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)

            viewHolder.itemView.alpha = 1.0f
        }
    }

    object Constants {
        const val DB_NAME = "PublistDb"
        const val SPLASH_DELAY: Long = 2000
        const val MINIMUM_SELECTED_CATEGORIES = 1
        const val MAXIMUM_SELECTED_CATEGORIES = 5
        const val MAX_VISIBLE_TODOS = 3
        const val PLATFORM_GOOGLE = "google"
        const val PLATFORM_FACEBOOK = "facebook"
        const val EMAIL_PERMISSION = "email"
        const val PROFILE_PICTURE_PERMISSION = "public_profile"
        const val USERS_COLLECTION_PATH = "users"
        const val USER_ACCOUNTS_COLLECTION_PATH = "userAccounts"
        const val MY_CATEGORIES_COLLECTION_PATH = "myCategories"
        const val MY_FAVORITES_COLLECTION_PATH = "myFavorites"
        const val MY_LISTS_COLLECTION_PATH = "myLists"
        const val CATEGORIES_COLLECTION_PATH = "categories"
        const val WISHES_COLLECTION_PATH = "wishes"
        const val PROFILE_PICTURE_URL_FIELD = "profilePictureUrl"
        const val EMAIL_FIELD = "email"
        const val NAME_FIELD = "name"
        const val DATE_FIELD = "date"
        const val CATEGORY_ID_FIELD = "categoryId"
        const val FIND_ACTION = "find"
        const val SAVE_ACTION = "save"
    }
}
