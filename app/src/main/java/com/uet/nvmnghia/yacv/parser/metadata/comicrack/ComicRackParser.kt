package com.uet.nvmnghia.yacv.parser.metadata.comicrack

import android.util.Xml
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.metadata.INonGenericMetadataParser
import com.uet.nvmnghia.yacv.utils.StringUtils.Companion.normalizeSpaces
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.util.*


/**
 * Parse ComicRack `ComicInfo.xml`.
 * Example xml structure, from https://wiki.mobileread.com/wiki/ComicRack
 * is stored in `ComicInfo.xml`.
 * Full schema from https://gist.github.com/vaemendis/9f3ed374f215532d12bda3e812a130e6
 * is stored in `ComicInfo.xsd`
 */
class ComicRackParser: INonGenericMetadataParser {
    override fun isParsableByName(metadataFilename: String?, lowered: Boolean): Boolean {
        if (metadataFilename == null) {
            return false
        }

        return if (lowered) {
            "comicinfo.xml" == metadataFilename
        } else {
            "comicinfo.xml" == metadataFilename.toLowerCase(Locale.ROOT)
        }
    }

    override fun parse(mis: InputStream, comic: Comic): Boolean {
        try {
            mis.use {
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                parser.setInput(mis, null)
                parser.nextTag()
                parseComicTag(parser, comic)
            }

            comic.nonGenericallyParsed = true

            return true
        } catch (xppe: XmlPullParserException) {
        } catch (ioe: IOException) {
        }

        return false
    }

    /**
     * Parse <ComicInfo>.
     * Parser should already be at <ComicInfo>.
     *
     * @param parser [XmlPullParser] that is already at <ComicInfo>
     * @param comic The [Comic] instance that will be populated
     */
    private fun parseComicTag(parser: XmlPullParser, comic: Comic) {
        assertOpenTag(parser, "ComicInfo")

        var year: Int? = null
        var month = 1
        var day = 1

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            // TODO: <Pages> are ignored for now
            when (parser.name) {
                // @formatter:off
                "Title"         -> comic.title         = parseText(parser)
                "Number"        -> comic.number        = parseInt(parser)
                "Summary"       -> comic.summary       = parseText(parser)
                "LanguageISO"   -> comic.language      = parseText(parser)
                "Publisher"     -> comic.publisher     = parseText(parser)
                "BlackAndWhite" -> comic.bw            = parseBool(parser)
                "Manga"         -> comic.tmpManga      = parseBool(parser)
                "Year"          -> year                = parseInt(parser)
                "Month"         -> month               = parseInt(parser) ?: month
                "Day"           -> day                 = parseInt(parser) ?: day
                "Web"           -> comic.web           = parseText(parser)
                "Series"        -> comic.tmpSeries     = parseText(parser)
                "Volume"        -> comic.tmpVolume     = parseInt(parser)
                "Count"         -> comic.tmpCount      = parseInt(parser)
                "Characters"    -> comic.tmpCharacters = parseText(parser)
                "Writer"        -> comic.tmpWriter     = parseText(parser)
                "Penciller"     -> comic.tmpPenciller  = parseText(parser)
                "Inker"         -> comic.tmpInker      = parseText(parser)
                "Colorist"      -> comic.tmpColorist   = parseText(parser)
                "Letterer"      -> comic.tmpLetterer   = parseText(parser)
                "Editor"        -> comic.tmpEditor     = parseText(parser)
                "Genre"         -> comic.tmpGenre      = parseText(parser)
                else            -> skip(parser)
                // @formatter:on
            }
        }

        if (year != null && year > 0) {
            val date = Calendar.getInstance()
            date.set(Calendar.YEAR, year)

            --month    // JSR-310 is for 26+ only :(
            if (month < Calendar.JANUARY || month > Calendar.DECEMBER) {
                month = Calendar.JANUARY
            }
            date.set(Calendar.MONTH, month)

            if (day < 1 || day > date.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                day = 1
            }
            date.set(Calendar.DAY_OF_MONTH, day)

            comic.date = date
        }
    }

    /**
     * Given a tag, check if the current parsing position is at the tag's opening.
     * Throw an exception if not.
     *
     * @param parser [XmlPullParser] to check
     * @param tag The name of the tag to check
     */
    private fun assertOpenTag(parser: XmlPullParser, tag: String) {
        parser.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, tag)
    }

    /**
     * Parse the text, also normalize spaces.
     *
     * @param parser [XmlPullParser] that is right before a text body
     * @return The parsed text, empty if the parser is not right before any text body
     */
    private fun parseText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return normalizeSpaces(result)
    }

    private val SET_OF_BOOL_TRUE = setOf("yes", "true", "1")

    /**
     * Parse the text as bool.
     * Invalid values are treated as false.
     *
     * @param parser [XmlPullParser] that is right before a text body storing a bool
     * @return The parsed bool; invalid values are treated as false
     */
    private fun parseBool(parser: XmlPullParser): Boolean {
        return parseText(parser).toLowerCase(Locale.ROOT) in SET_OF_BOOL_TRUE
    }

    /**
     * Parse the text as integer.
     * Invalid values are treated as null.
     *
     * @param parser [XmlPullParser] that is right before a text body storing a bool
     * @return The parsed integer; invalid values are treated as null
     */
    private fun parseInt(parser: XmlPullParser): Int? {
        return try {
            parseText(parser).toInt()
        } catch (nfe: NumberFormatException) {
            null
        }
    }

    /**
     * Skip tags.
     */
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1

        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}