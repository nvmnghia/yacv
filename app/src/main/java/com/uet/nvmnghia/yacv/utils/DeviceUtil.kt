package com.uet.nvmnghia.yacv.utils

import android.content.Context


class DeviceUtil {
    companion object {
        fun getScreenWidthInPx(ctx: Context): Int {
            val metrics = ctx.resources.displayMetrics
            return metrics.widthPixels
        }
    }
}