package com.konovus.myfiles.ui.pagerScreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.konovus.myfiles.R
import com.konovus.myfiles.entities.MyData

class PagerFragment : Fragment(R.layout.pager_fragment) {

    private val args by navArgs<PagerFragmentArgs>()
    private lateinit var data: MyData

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pager_fragment, container, false)

        data = args.data
        val position = args.position
        (requireActivity() as AppCompatActivity).supportActionBar?.title = data.flatten()[position].name


        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = MyPagerAdapter(this)
        viewPager.setCurrentItem(position, false)
        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                (requireActivity() as AppCompatActivity).supportActionBar?.title = data.flatten()[position].name
            }
        })


        return view
    }


    private inner class MyPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = data.flatten().size
        override fun createFragment(position: Int): Fragment = ImagePagerItemFragment.newInstance(
            data.flatten()[position])

    }
}

