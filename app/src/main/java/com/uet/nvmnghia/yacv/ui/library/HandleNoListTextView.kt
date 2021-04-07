package com.uet.nvmnghia.yacv.ui.library

import android.content.res.Resources
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
        var SPAN_TEXT_COLOR: Int? = null

        fun noRoot(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_root),
                res.getString(R.string.no_root_span_part),
                clickCallback
            )
        }

        fun noComic(res: Resources, clickCallback: () -> Any): SpannableString {
            return oneSpannableText(
                res.getString(R.string.no_comic),
                res.getString(R.string.no_comic_span_part),
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
                    ds.color = SPAN_TEXT_COLOR!!
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