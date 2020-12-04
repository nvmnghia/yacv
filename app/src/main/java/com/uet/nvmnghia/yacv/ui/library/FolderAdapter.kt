package com.uet.nvmnghia.yacv.ui.library

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uet.nvmnghia.yacv.R
import com.uet.nvmnghia.yacv.model.folder.Folder


class FolderAdapter : ListAdapter<Folder, FolderAdapter.ViewHolder>(DIFF_CALLBACK) {

    //================================================================================
    // Adapter functions
    //================================================================================

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.library_item_folder, parent,
                false)    // not attach to parent so that parent doesn't receive touch event

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = getItem(position).path
    }

    //================================================================================
    // ViewHolder
    //================================================================================

    // Note that RecyclerView.ViewHolder(view) is a method call,
    // furthermore, a constructor call
    // This line delegates the constructor to RecyclerView.ViewHolder(view)
    // i.e. a shorter syntax for super(view)
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_folder_text)
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<Folder> = object : DiffUtil.ItemCallback<Folder>() {
            override fun areItemsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean {
                return oldItem.path == newItem.path
            }
        }
    }
}