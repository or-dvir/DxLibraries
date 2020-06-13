package com.hotmail.or_dvir.dxlibraries.draggable

import android.view.View
import android.view.ViewGroup

import com.hotmail.or_dvir.dxlibraries.BaseSampleAdapter
import com.hotmail.or_dvir.dxlibraries.R

class AdapterNonDraggable(mItems: MutableList<ItemNonDraggable>) :
    BaseSampleAdapter<ItemNonDraggable, AdapterDraggable.ViewHolder>(mItems) {

    override fun createAdapterViewHolder(itemView: View, parent: ViewGroup, viewType: Int) =
        AdapterDraggable.ViewHolder(itemView)

    override fun getItemLayoutRes(parent: ViewGroup, viewType: Int) =
        R.layout.list_item_draggable

    override fun onBindViewHolder(holder: AdapterDraggable.ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        holder.tv.text = item.text
    }
}