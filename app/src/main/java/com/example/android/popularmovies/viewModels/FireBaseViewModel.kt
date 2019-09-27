package com.example.android.popularmovies.viewModels

import android.util.Log
import com.example.android.popularmovies.Navigators.FireBaseNavigator
import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel
import com.example.android.popularmovies.utilities.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.*

class FireBaseViewModel(navigator: FireBaseNavigator?) {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var navigator: FireBaseNavigator? = navigator
    private fun addMovie(movie: FirebaseMovieModel) {
        var documentName = "${movie.name}(${movie.year})".toLowerCase()
        val fireBaseUser = hashMapOf(
                Constant.MOVIE_LINK_KEY_FIREBASE to movie.link,
                Constant.MOVIE_SUB_KEY_FIREBASE to movie.sub,
                Constant.MOVIE_NAME_KEY_FIREBASE to movie.name,
                Constant.MOVIE_YEAR_KEY_FIREBASE to movie.year,
                Constant.MOVIE_RATE_KEY_FIREBASE to movie.movieRate
                )

        val db = FirebaseFirestore.getInstance()
        db.collection(Constant.MOVIE_COLLECTION_KEY).document(documentName).set(fireBaseUser)
                .addOnSuccessListener { documentReference ->

                    //                    print("adding currentUser")

                }.addOnFailureListener { ex ->

                }

    }

    private fun getMovieYear(releaseDate: String): String {
        var temp = releaseDate.split("-")
        return temp[0]
    }

    fun getMovieByName(movieName: String, movieYear: String,movieRate: String) {
        var movieYear = getMovieYear(movieYear)
        var documentName = "${movieName}(${movieYear})".toLowerCase()
        var movieName = movieName.toLowerCase()
        val db = FirebaseFirestore.getInstance()
        if (!documentName.matches(Regex(".*[@#$%^&*/].*"))) {


            db.collection(Constant.MOVIE_COLLECTION_KEY).document(documentName).get().addOnSuccessListener { documents ->

                if (documents.get(Constant.MOVIE_LINK_KEY_FIREBASE) != null || documents.get(Constant.MOVIE_SUB_KEY_FIREBASE) != null) {

                    var currentMovie: FirebaseMovieModel? = FirebaseMovieModel(documents.get(Constant.MOVIE_LINK_KEY_FIREBASE) as String,
                            documents.get(Constant.MOVIE_SUB_KEY_FIREBASE) as String,
                    documents.get(Constant.MOVIE_NAME_KEY_FIREBASE) as String,
                    documents.get(Constant.MOVIE_YEAR_KEY_FIREBASE) as String,
                    documents.get(Constant.MOVIE_RATE_KEY_FIREBASE) as String)
                    if (currentMovie?.link != "" || currentMovie?.sub != "")
                        navigator?.onMovieFounded(currentMovie)
//                    else
//                        navigator?.onNotFound()
                }
                else
                    addMovie(FirebaseMovieModel("", "", movieName, movieYear,movieRate))

            }.addOnFailureListener { exception ->
                //navigator?.onNotFound()
            }
        }
    }
}