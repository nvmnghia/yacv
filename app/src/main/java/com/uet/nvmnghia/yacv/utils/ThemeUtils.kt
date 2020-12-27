package com.uet.nvmnghia.yacv.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R


class ThemeUtils {
    companion object {
        fun getAccentColor(context: Context): Int {
            return getStyledAttribute(context, R.attr.colorAccent)
        }

        fun getPrimaryColor(context: Context): Int {
            return getStyledAttribute(context, R.attr.colorPrimary)
        }

        private fun getStyledAttribute(context: Context, attributeId: Int): Int {
            val tv = TypedValue()
            val ta: TypedArray = context.obtainStyledAttributes(
                tv.data, intArrayOf(attributeId))
            val color = ta.getColor(0, 0)
            ta.recycle()
            return color
        }

        /**
         * Given the [spacing] value in pixel, returns a [RecyclerView.ItemDecoration]
         * that has right and bottom spacing both set to [spacing].
         */
        fun getRightBottomSpacer(spacing: Int): RecyclerView.ItemDecoration {
            return object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State
                ) {
                    outRect.right  = spacing
                    outRect.bottom = spacing
                }
            }
        }
    }
}