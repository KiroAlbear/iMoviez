package com.example.android.popularmovies.ui.main

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompatSideChannelService
import androidx.databinding.DataBindingUtil
import com.example.android.popularmovies.R
import com.example.android.popularmovies.databinding.ActivityPlayerBinding
import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel
import com.example.android.popularmovies.utilities.Constant
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import com.google.firebase.database.core.view.View
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL

class PlayerActivity : AppCompatActivity() {

    lateinit var binding: ActivityPlayerBinding
    lateinit var movie: FirebaseMovieModel
    lateinit var downloadPath: File
    lateinit var pageUrl: String
    lateinit var subtitleURL: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player)

        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        movie = intent.getSerializableExtra(Constant.INTENT_MOVIE_KEY) as FirebaseMovieModel



        Thread(Runnable {
            pageUrl = getMovieUrl(movie.link)
            subtitleURL = getMovieUrl(movie.sub)

            runOnUiThread(Runnable {
                initExoPlayer()
            })

        }).start()

        // full screen flag
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // screen alwayes on flag
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayer.player.playWhenReady = false
    }

    fun initExoPlayer() {
        var mergedSource: MergingMediaSource
        downloadPath = Environment.getExternalStorageDirectory()
        val player = ExoPlayerFactory.newSimpleInstance(this)
        binding.videoPlayer.player = player
        val dataSourceFactory = DefaultDataSourceFactory(
                this,
                Util.getUserAgent(this, "yourApplicationName")
        )

        val subtitleFormat = Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                0, // Selection flags for the track.
                null
        )

        var videoSource: MediaSource =
                ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                        Uri.parse(pageUrl)
                )


        val subtitleSource = SingleSampleMediaSource(
                Uri.parse(subtitleURL), dataSourceFactory,
                Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP, Format.NO_VALUE, "en", null),
                C.TIME_UNSET
        )

        if (subtitleURL == "")
            mergedSource = MergingMediaSource(videoSource)
        else
            mergedSource = MergingMediaSource(videoSource, subtitleSource)



        player.prepare(mergedSource)
        player.setPlayWhenReady(true)


        binding.videoPlayer.getSubtitleView().setApplyEmbeddedStyles(false)
        binding.videoPlayer.getSubtitleView().setApplyEmbeddedFontSizes(false)
        binding.videoPlayer.getSubtitleView().setStyle(
                CaptionStyleCompat(
                        Color.WHITE,
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        Color.BLACK,
                        null
                )
        )

        binding.videoPlayer.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, 85f);

        binding.videoPlayer.player.addListener(object : Player.DefaultEventListener() {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    binding.progressBar.visibility = android.view.View.INVISIBLE
                }
            }

        })


    }

    fun getMovieUrl(downloadURL: String): String {
        var ghr: BufferedReader? = null
        var foundUrl = ""
        var sgh = StringBuilder()
        if (downloadURL != "") {
            try {

                var url = URL(downloadURL)
                ghr = BufferedReader(InputStreamReader(url.openStream()))

                var tempLine: String? = ""
                var originalLine: String? = ""


                while (tempLine != null) {
                    originalLine = ghr.readLine()
                    tempLine = originalLine.toLowerCase().replace('.', ' ').replace('+', ' ')
                    if (tempLine.contains(movie.name) && tempLine.contains("href") && !tempLine.contains("file")) {
                        foundUrl = originalLine
                        foundUrl = foundUrl.substringAfter('"')
                        foundUrl = foundUrl.substringBefore('"')
                        break
                    }
                }

                System.out.println(sgh)
            } finally {

                if (ghr != null) {
                    ghr.close()
                }
            }
        }

        return foundUrl

    }
}
