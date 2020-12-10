package com.uet.nvmnghia.yacv.ui.library

import android.content.res.Resources
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.uet.nvmnghia.yacv.R


/**
 * Create spannable text here to avoid cluttering [LibraryFragment].
 */
class HandleNoListTextView {
    companion object {
        fun noRootFolder(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_root_folder_selected),
                res.getString(R.string.no_root_folder_selected_spannable_part),
                clickCallback
            )
        }

        fun cannotReadRootFolder(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.cannot_read_root_folder),
                res.getString(R.string.cannot_read_root_folder_spannable_part),
                clickCallback
            )
        }

        fun noReadPermission(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_read_permission),
                res.getString(R.string.no_read_permission_spannable_part),
                clickCallback
            )
        }

        fun noReadPermissionTwice(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_read_permission_twice),
                res.getString(R.string.no_read_permission_twice_spannable_part),
                clickCallback
            )
        }

        fun noComic(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_comic),
                res.getString(R.string.no_comic_spannable_part),
                clickCallback
            )
        }

        /**
         * Create a Spannable text with 1 clickable part,
         * then set it as the text of the given TextView.
         */
        private fun oneSpannableText(content: String, spannablePart: String, clickCallback: () -> Any): SpannableString {
            val spannableContent = SpannableString(content)

            // TODO: use Folder icon instead
            // https://stackoverflow.com/questions/25521685/how-to-insert-drawables-in-text

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    clickCallback()
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = Color.BLUE
                    ds.isUnderlineText = true
                }
            }

            val spannablePartIdx = content.lastIndexOf(spannablePart)
            spannableContent.setSpan(clickableSpan,
                spannablePartIdx, spannablePartIdx + spannablePart.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            return spannableContent
        }
    }
}