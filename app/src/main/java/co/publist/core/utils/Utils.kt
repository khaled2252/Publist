package co.publist.core.utils

import android.app.Activity
import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

object Utils {

    fun hideSoftKeyboard(context: Context, iBinder: IBinder) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(iBinder, 0)
    }

    @JvmStatic
    @BindingAdapter("profilePictureUrl")
    fun loadProfilePicture(view: ImageView, imageUrl: String?) {
        val circularProgressDrawable = CircularProgressDrawable(view.context)
        circularProgressDrawable.strokeWidth = 5f
        circularProgressDrawable.centerRadius = 30f
        circularProgressDrawable.start()
        Glide.with(view)
            .load(imageUrl)
            .placeholder(circularProgressDrawable)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }

    object Constants{
        const val SPLASH_DELAY :Long = 2000
        const val MINIMUM_SELECTED_CATEGORIES = 1
        const val MAXIMUM_SELECTED_CATEGORIES = 5
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
        const val PROFILE_PICTURE_URL_FIELD = "profilePictureUrl"
        const val EMAIL_FIELD = "email"
        const val NAME_FIELD = "name"
        const val FIND_ACTION = "find"
        const val SAVE_ACTION = "save"
    }
}
