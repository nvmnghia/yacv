package com.uet.nvmnghia.yacv.ui.metadata

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.glide.TopCrop
import com.uet.nvmnghia.yacv.model.comic.Comic
import com.uet.nvmnghia.yacv.parser.file.ComicParser
import com.uet.nvmnghia.yacv.utils.FileUtils
import com.uet.nvmnghia.yacv.utils.StringUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException


/**
 * A simple [Fragment] subclass.
 * Use the [MetadataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class MetadataFragment : Fragment() {

    val viewModel: MetadataViewModel by viewModels()

    val args: MetadataFragmentArgs by navArgs()

    private lateinit var glide: RequestManager

    private lateinit var coverImg:      ImageView
    private lateinit var fileNameTxt:   TextView
    private lateinit var folderPathTxt: TextView
    private lateinit var readCountTxt:  TextView
    private lateinit var loveImg:       ImageView
    private lateinit var titleTxt:      TextView
    private lateinit var numberTxt:     TextView
    private lateinit var seriesTxt:     TextView
    private lateinit var volumeTxt:     TextView
    private lateinit var authorsTxt:    TextView
    private lateinit var summaryTxt:    TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setComicID(args.comicID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_metadata, container, false)

        // TODO: set this earlier
        // https://stackoverflow.com/a/60130631/5959593
        viewModel.comic.observe(viewLifecycleOwner, this::showMetadata)

        glide = Glide.with(requireContext())
            .setDefaultRequestOptions(
                RequestOptions.formatOf(DecodeFormat.PREFER_RGB_565)
            )

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            resources.getString(R.string.metadata)

        setupViews(view)

        return view
    }

    private fun setupViews(root: View) {
        coverImg      = root.findViewById(R.id.metadata_cover)
        fileNameTxt   = root.findViewById(R.id.metadata_filename)
        folderPathTxt = root.findViewById(R.id.metadata_folder_path)
        readCountTxt  = root.findViewById(R.id.metadata_read_count)
        loveImg       = root.findViewById(R.id.metadata_love)
        titleTxt      = root.findViewById(R.id.metadata_title)
        numberTxt     = root.findViewById(R.id.metadata_number)
        seriesTxt     = root.findViewById(R.id.metadata_series)
        volumeTxt     = root.findViewById(R.id.metadata_volume)
        authorsTxt    = root.findViewById(R.id.metadata_authors)
        summaryTxt    = root.findViewById(R.id.metadata_summary)
    }

    private fun showMetadata(comic: Comic) {
        val unknownPlaceholder = resources.getString(R.string.unknown)
        val pathPart = Uri.parse(comic.fileUri).path!!

        loadCover(comic.fileUri)
        fileNameTxt.text   = StringUtils.nameFromPath(pathPart)
        folderPathTxt.text = "Kileko"
        readCountTxt.text  = readCountText(comic.readCount)
        setLove(comic.love)
        titleTxt.text      = comic.title
        numberTxt.text     = comic.number?.toString() ?: unknownPlaceholder
        summaryTxt.text    = comic.summary ?: unknownPlaceholder
    }

    private fun loadCover(fileUri: String) {
        // Copypasta from ComicAdapter
        CoroutineScope(Dispatchers.IO).launch {
            val parser = ComicParser(requireContext(), fileUri)
            val coverRequest = parser.requestCover()

            withContext(Dispatchers.Main) {
                glide.load(coverRequest)
                    .transform(TopCrop())
                    .into(coverImg)
            }
        }
    }

    private fun readCountText(readCount: Int): String =
        when  {
            readCount < 0  -> throw IllegalArgumentException("readCount = $readCount < 0")
            readCount == 0 -> resources.getString(R.string.read_0)
            readCount < 10 -> resources.getString(R.string.read_some, readCount)
            else -> resources.getString(R.string.read_many)
        }

    private fun setLove(love: Boolean) =
        loveImg.setImageResource(
            if (love) R.drawable.ic_baseline_favorite_24
            else R.drawable.ic_baseline_favorite_border_24)

}