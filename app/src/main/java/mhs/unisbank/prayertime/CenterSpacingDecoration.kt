package mhs.unisbank.prayertime

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class CenterSpacingDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        when (position) {
            0 -> outRect.set(space, 0, space / 2, 0) // First item
            itemCount - 1 -> outRect.set(space / 2, 0, space, 0) // Last item
            else -> outRect.set(space / 2, 0, space / 2, 0)
        }
    }
}