package com.publist.core.utils

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.publist.R
import com.squareup.picasso.Picasso

object DataBindingAdapters {

    @JvmStatic
    @BindingAdapter("profilePictureUrl")
    fun loadProfilePicture(view: ImageView, imageUrl: String?) {
        Glide.with(view.context.applicationContext)
            .load(imageUrl)
            .placeholder(R.drawable.ic_guest)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
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
        } else
            view.visibility = View.GONE
    }
}