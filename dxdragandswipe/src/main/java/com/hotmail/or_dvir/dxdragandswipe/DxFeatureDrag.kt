package com.hotmail.or_dvir.dxdragandswipe

import android.view.MotionEvent
import android.view.View
import androidx.annotation.IdRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.hotmail.or_dvir.dxadapter.DxAdapter
import com.hotmail.or_dvir.dxadapter.IDxBaseFeature

class DxFeatureDrag(
    private val onDragStart: onItemDragSwipeInteractionListener,
    private val onDragEnd: onItemDragSwipeInteractionListener,
    val onItemMoved: onItemMovedListener,
    val dragOnLongClick: Boolean = false
) : IDxBaseFeature {

    @IdRes internal var dragHandleId: Int? = null
    internal var itemTouchHelper: ItemTouchHelper? = null

    var isDragEnabled = true
    internal var flagIsDragging = false

    override fun onCreateViewHolder(
        adapter: DxAdapter<*>,
        itemView: View,
        holder: RecyclerView.ViewHolder
    ) {
        val item = adapter.getDxAdapterItems()[holder.adapterPosition]
        if (item !is IDxItemDraggable || !isDragEnabled || dragHandleId == null) {
            return
        }

        itemView.findViewById<View>(dragHandleId!!).setOnTouchListener { view, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    signalDragStart(itemView, holder)
                    //signalDragStart is also called from DxItemTouchCallback but
                    //we only want to manually start the drag operation from here
                    itemTouchHelper?.startDrag(holder)
                }
                MotionEvent.ACTION_UP -> signalDragEnd(itemView, holder)
            }

            //allow normal processing to continue
            false
        }
    }

    internal fun signalDragStart(itemView: View, holder: RecyclerView.ViewHolder) {
        flagIsDragging = true
        onDragStart.invoke(itemView, holder.adapterPosition)
    }

    internal fun signalDragEnd(itemView: View, holder: RecyclerView.ViewHolder) {
        flagIsDragging = false
        onDragEnd.invoke(itemView, holder.adapterPosition)
    }
}