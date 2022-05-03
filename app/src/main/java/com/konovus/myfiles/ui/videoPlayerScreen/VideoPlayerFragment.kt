package com.konovus.myfiles.ui.videoPlayerScreen

import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.konovus.myfiles.MainActivity
import com.konovus.myfiles.R
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.*
import com.konovus.myfiles.TAG
import androidx.appcompat.app.AppCompatActivity





class VideoPlayerFragment : Fragment(R.layout.video_player_fragment) {

    private val args by navArgs<VideoPlayerFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MainActivity.tabLayout.visibility = View.GONE

        val path = args.path
        val videoView = view.findViewById<VideoView>(R.id.video_view)
        videoView.setVideoURI(Uri.parse(path))
        val mediaController = MediaController(requireContext())
        videoView.setMediaController(mediaController)
        mediaController.setAnchorView(videoView)
        videoView.start()
        setDimension(videoView, path)

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            hides status bar
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            hides appBar
            (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        }

    }

    private fun setDimension(videoView: VideoView, path: String){
        // Adjust the size of the video
        // so it fits on the screen
        val videoProportion = getVideoProportion(path)
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenHeight.toFloat() / screenWidth.toFloat()
        Log.i(TAG, "setDimension: $screenHeight / $screenWidth = $screenProportion")
        val lp: ViewGroup.LayoutParams = videoView.layoutParams
        if (videoProportion > screenProportion && videoProportion > 1) {
            lp.height = screenHeight
            lp.width = (screenWidth / videoProportion).toInt()
        } else {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() * videoProportion).toInt()
        }
        videoView.layoutParams = lp
    }

    // This method gets the proportion of the video that you want to display.
    // I already know this ratio since my video is hardcoded, you can get the
    // height and width of your video and appropriately generate  the proportion
    //    as :height/width
    private fun getVideoProportion(path: String) : Float {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        val frame = retriever.frameAtTime!!
        val width = frame.width.toFloat()
//            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toFloat()
        val height = frame.height.toFloat()
//            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toFloat()
        retriever.release()
        Log.i(TAG, "getVideoProportion: $height/$width =  ${(height/width)}")
        return  height/width
    }
}