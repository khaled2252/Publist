package com.publist.features.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.publist.R
import com.publist.features.onboarding.SliderAdapter.SliderAdapterVH
import com.smarteist.autoimageslider.SliderViewAdapter
import kotlinx.android.synthetic.main.image_slider_layout_item.view.*
import java.util.*

class SliderAdapter(imagesResourceIds: ArrayList<Int>?) :
    SliderViewAdapter<SliderAdapterVH>() {

    private val imageResourceIds = ArrayList<Int>()

    init {
        imageResourceIds.addAll(imagesResourceIds!!)
    }

    override fun getCount(): Int {
        return imageResourceIds.size
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val inflate = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_slider_layout_item, parent, false)
        return SliderAdapterVH(inflate)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        viewHolder.itemView.sliderImageView.setImageResource(imageResourceIds[position])
    }

    inner class SliderAdapterVH(itemView: View) : ViewHolder(itemView)

}