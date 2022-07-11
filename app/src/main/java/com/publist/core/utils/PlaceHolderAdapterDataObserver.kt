package com.publist.core.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver


class PlaceHolderAdapterDataObserver(
    private val adapter: RecyclerView.Adapter<*>,
    private val placeHolderView: View
) : AdapterDataObserver() {
    private fun checkIfEmpty() {
        if (adapter.itemCount == 0) {
            placeHolderView.visibility = View.VISIBLE
        } else {
            placeHolderView.visibility = View.GONE
        }
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        checkIfEmpty()
    }

}