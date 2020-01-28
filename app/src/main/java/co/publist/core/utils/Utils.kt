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
}
