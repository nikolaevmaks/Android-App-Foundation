package com.application.foundation.features.common.view.utils

import android.graphics.Rect
import android.os.Parcelable
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.parcelize.Parcelize

object RecyclerViewUtils {

	private val decorations = Rect()
	private val scrollCached = ScrollPosition()

	@JvmStatic
	fun getScrollPosition(list: RecyclerView): ScrollPosition {
		return getScrollPosition(list, true)
	}

	@JvmStatic
	fun getScrollPosition(list: RecyclerView, returnCachedResult: Boolean): ScrollPosition {

		val scroll = if (returnCachedResult) scrollCached else ScrollPosition()

		val layoutManager = list.layoutManager

		scroll.firstVisibleItemPosition = if (layoutManager is LinearLayoutManager) layoutManager.findFirstVisibleItemPosition() else
				(layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

		if (scroll.firstVisibleItemPosition == RecyclerView.NO_POSITION) {
			scroll.firstVisibleItemPosition = 0
		} else {
			val holder = list.findViewHolderForAdapterPosition(scroll.firstVisibleItemPosition)

			if (holder != null) {
				list.getDecoratedBoundsWithMargins(holder.itemView, decorations)

				if (layoutManager is LinearLayoutManager) {
					scroll.scrollOffset = if (layoutManager.orientation == LinearLayoutManager.HORIZONTAL) decorations.left - list.paddingLeft else decorations.top - list.paddingTop
				} else {
					scroll.scrollOffset = if ((layoutManager as GridLayoutManager).orientation == LinearLayoutManager.HORIZONTAL) decorations.left - list.paddingLeft
							else decorations.top - list.paddingTop
				}
			}
		}
		return scroll
	}

	@JvmStatic
	fun getScrollPositionWithoutOffset(list: RecyclerView): Int {
		val layoutManager = list.layoutManager

		val firstVisibleItemPosition = if (layoutManager is LinearLayoutManager) layoutManager.findFirstVisibleItemPosition() else
				(layoutManager as GridLayoutManager).findFirstVisibleItemPosition()

		return if (firstVisibleItemPosition == RecyclerView.NO_POSITION) 0 else firstVisibleItemPosition
	}

	@JvmStatic
	fun setScrollPosition(list: RecyclerView, scroll: ScrollPosition) {
		setScrollPosition(list, scroll.firstVisibleItemPosition, scroll.scrollOffset)
	}

	@JvmStatic
	fun setScrollPosition(list: RecyclerView, firstVisibleItemPosition: Int, scrollOffset: Int) {

		list.stopScroll()
		val layoutManager = list.layoutManager

		if (layoutManager is LinearLayoutManager) {
			layoutManager.scrollToPositionWithOffset(firstVisibleItemPosition, scrollOffset)

		} else if (layoutManager is GridLayoutManager) {
			layoutManager.scrollToPositionWithOffset(firstVisibleItemPosition, scrollOffset)
		}
	}

	//androidx.recyclerview.widget.LinearLayoutManager
	// public void scrollToPosition(int position)
	// RecyclerView will scroll the minimum amount that is necessary to make the target position visible.
	// If you are looking for a similar behavior to android.widget.ListView.setSelection(int) or android.widget.ListView.setSelectionFromTop(int, int), use scrollToPositionWithOffset(int, int).
	// not work as expected, use setScrollPosition(RecyclerView list, int firstVisibleItemPosition, int scrollOffset)
	@JvmStatic
	fun setScrollPosition(list: RecyclerView, position: Int, smooth: Boolean) {
		list.stopScroll()

		if (smooth) {
			list.smoothScrollToPosition(position)
		} else {
			list.scrollToPosition(position)
		}
	}

	@JvmStatic
	fun resetScrolling(list: RecyclerView) {
		list.stopScroll()
		list.scrollToPosition(0)
	}

	@JvmStatic
	fun smoothResetScrolling(list: RecyclerView) {
		list.stopScroll()
		list.smoothScrollToPosition(0)
	}

	@JvmStatic
	fun notifyItemRangeChangedBeyondVisibleItems(list: RecyclerView, adapter: RecyclerView.Adapter<*>) {

		if (list.childCount != 0) {
			var minAdapterPos = Int.MAX_VALUE
			var maxAdapterPos = 0

			for (i in 0 until list.childCount) {
				val holder = list.getChildViewHolder(list.getChildAt(i))

				val adapterPos = holder.adapterPosition
				if (holder.layoutPosition != adapterPos) {
					// inconsistency
					return
				}

				minAdapterPos = Math.min(minAdapterPos, adapterPos)
				maxAdapterPos = Math.max(maxAdapterPos, adapterPos)
			}

			adapter.notifyItemRangeChanged(0, minAdapterPos)
			adapter.notifyItemRangeChanged(maxAdapterPos + 1, adapter.itemCount - (maxAdapterPos + 1))

		} else {
			adapter.notifyDataSetChanged()
		}
	}

	@JvmStatic
	fun checkInconsistency(list: RecyclerView, adapter: RecyclerView.Adapter<*>): Boolean {

		for (i in 0 until list.childCount) {
			val holder = list.getChildViewHolder(list.getChildAt(i))

			if (holder.layoutPosition != holder.adapterPosition) {
				adapter.notifyDataSetChanged()
				return true
			}
		}
		return false
	}

	@JvmStatic
	fun isScrolledToTop(list: RecyclerView): Boolean {
		val scroll = getScrollPosition(list)
		return scroll.firstVisibleItemPosition == 0 && scroll.scrollOffset == 0
	}

	@Parcelize
	class ScrollPosition (

		var firstVisibleItemPosition: Int = 0,
		var scrollOffset: Int = 0
	) : Parcelable {

		fun reset() {
			firstVisibleItemPosition = 0
			scrollOffset = 0
		}

		fun update(scroll: ScrollPosition) {
			firstVisibleItemPosition = scroll.firstVisibleItemPosition
			scrollOffset = scroll.scrollOffset
		}
	}
}