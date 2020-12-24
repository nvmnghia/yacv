package com.uet.nvmnghia.yacv.utils

import android.content.Context
import android.content.res.TypedArray
import android.util.TypedValue
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
    }
}