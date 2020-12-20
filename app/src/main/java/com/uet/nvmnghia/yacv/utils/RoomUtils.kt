package com.uet.nvmnghia.yacv.utils

import androidx.room.TypeConverter
import java.util.*


class RoomUtils {
    class CalendarConverter {
        /**
         * Convert timestamp in millisecond [msTimestamp] to [Calendar].
         */
        @TypeConverter
        fun fromTimestampToCalendar(msTimestamp: Long?): Calendar? {
            if (msTimestamp == null) {
                return null
            }
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = msTimestamp
            return calendar
        }

        /**
         * Convert [Calendar] to timestamp in millisecond.
         */
        @TypeConverter
        fun fromCalendarToTimestamp(calendar: Calendar?): Long? {
            return calendar?.timeInMillis
        }
    }
}