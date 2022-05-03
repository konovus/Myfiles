package com.konovus.myfiles.ui.pagerScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.konovus.myfiles.R
import com.konovus.myfiles.entities.MyFile


class ImagePagerItemFragment : Fragment(R.layout.image_pager_item_fragment){

    private var file: MyFile = MyFile()
    companion object {
        fun newInstance(_file: MyFile) = ImagePagerItemFragment().apply {
            arguments?.putParcelable("file", _file)
            file = _file
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.image_pager_item_fragment, container, false)
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        Glide.with(requireContext()).load(file.uri).into(imageView)



        return view
    }
}