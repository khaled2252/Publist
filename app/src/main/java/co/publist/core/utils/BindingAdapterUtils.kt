package co.publist.core.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import co.publist.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.squareup.picasso.Picasso

object BindingAdapterUtils {

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
}