package com.addmusictovideos.audiovideomixer.sk.model

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class RecyclerHelper<T, VH : RecyclerView.ViewHolder>(
    var list: ArrayList<T>,
    var mAdapter: RecyclerView.Adapter<VH>
) : ItemTouchHelper.Callback() {

    var onDragListener: OnDragListener? = null
    var onSwipeListener: OnSwipeListener? = null
    private var isItemDragEnabled: Boolean = false
    private var isItemSwipeEnabled: Boolean = false



    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val layoutManager = recyclerView.layoutManager
        val isHorizontal = (layoutManager as? LinearLayoutManager)?.orientation == LinearLayoutManager.HORIZONTAL

        val dragFlags = if (isItemDragEnabled) {
            if (isHorizontal) ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            else ItemTouchHelper.UP or ItemTouchHelper.DOWN
        } else 0

        val swipeFlags = if (isItemSwipeEnabled) {
            if (isHorizontal) ItemTouchHelper.UP or ItemTouchHelper.DOWN
            else ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        } else 0

        return makeMovementFlags(dragFlags, swipeFlags)
    }



    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition

        if (fromPosition == RecyclerView.NO_POSITION || toPosition == RecyclerView.NO_POSITION) {
            return false // Prevent crashes
        }

        // Swap items in the dataset
        Collections.swap(list, fromPosition, toPosition)

        // Notify adapter about the moved item
        mAdapter.notifyItemMoved(fromPosition, toPosition)

        // Notify the listener
        onDragListener?.onDragItemListener(fromPosition, toPosition)

        return true // Return true to indicate successful move
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            list.removeAt(position)
            mAdapter.notifyItemRemoved(position)
            onSwipeListener?.onSwipeItemListener()
        }
    }

    override fun isLongPressDragEnabled(): Boolean = isItemDragEnabled

    override fun isItemViewSwipeEnabled(): Boolean = isItemSwipeEnabled

    fun setRecyclerItemDragEnabled(isDragEnabled: Boolean): RecyclerHelper<T, VH> {
        this.isItemDragEnabled = isDragEnabled
        return this
    }

    fun setRecyclerItemSwipeEnabled(isSwipeEnabled: Boolean): RecyclerHelper<T, VH> {
        this.isItemSwipeEnabled = isSwipeEnabled
        return this
    }

    fun setOnDragItemListener(onDragListener: OnDragListener): RecyclerHelper<T, VH> {
        this.onDragListener = onDragListener
        return this
    }

    fun setOnSwipeItemListener(onSwipeListener: OnSwipeListener): RecyclerHelper<T, VH> {
        this.onSwipeListener = onSwipeListener
        return this
    }
}