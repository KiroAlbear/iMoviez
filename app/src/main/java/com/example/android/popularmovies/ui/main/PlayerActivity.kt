package com.example.android.popularmovies.ui.main

import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.example.android.popularmovies.R
import com.example.android.popularmovies.databinding.ActivityPlayerBinding
import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel
import com.example.android.popularmovies.utilities.Constant
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.text.CaptionStyleCompat
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.net.URLConnection
import java.util.*
import kotlin.concurrent.thread

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
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

    }

    override fun onPause() {
        super.onPause()
        binding.videoPlayer.player.playWhenReady = false
    }

    fun initExoPlayer() {

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


        val mergedSource = MergingMediaSource(videoSource, subtitleSource)

        player.prepare(mergedSource)
        player.setPlayWhenReady(true)


        binding.videoPlayer.getSubtitleView().setApplyEmbeddedStyles(false);
        binding.videoPlayer.getSubtitleView().setApplyEmbeddedFontSizes(false);
        binding.videoPlayer.getSubtitleView().setStyle(
                CaptionStyleCompat(
                        Color.RED,
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_OUTLINE,
                        Color.BLACK,
                        null
                )
        )

        binding.videoPlayer.getSubtitleView().setFixedTextSize(TypedValue.COMPLEX_UNIT_PX, 100f);

//        var params = LinearLayout.LayoutParams
//        binding.videoPlayer.getLayoutParams();
//        params.width=params.MATCH_PARENT;
//        params.height=params.MATCH_PARENT;
//        binding.videoPlayer.setLayoutParams(params);


    }

    fun getMovieUrl(downloadURL: String): String {
        var ghr: BufferedReader? = null
        var foundUrl = ""
        var sgh = StringBuilder()
        try {

            var url = URL(downloadURL)
            ghr = BufferedReader(InputStreamReader(url.openStream()))

            var line: String? = ""


            while (line != null) {
                line = ghr.readLine().toLowerCase()

                if (line.contains(movie.name) && line.contains("href") && !line.contains("file")) {
                    foundUrl = line
                    foundUrl = foundUrl.substringAfter('"')
                    foundUrl = foundUrl.substringBefore('"')
                    break
                }
            }

            System.out.println(sgh);
        } finally {

            if (ghr != null) {
                ghr.close();
            }
        }


        return foundUrl
    }
}
