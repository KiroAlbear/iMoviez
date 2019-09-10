package com.example.android.popularmovies.viewModels

import android.util.Log
import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel
import com.example.android.popularmovies.utilities.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.collections.*

class FireBaseViewModel {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun addMovie(movieName: String, movie: FirebaseMovieModel) {

        val fireBaseUser = hashMapOf(
                Constant.MOVIE_LINK_KEY_FIREBASE to movie.link,
                Constant.MOVIE_SUB_KEY_FIREBASE to movie
        )

        val db = FirebaseFirestore.getInstance()
        db.collection(Constant.MOVIE_COLLECTION_KEY).document(movieName).set(fireBaseUser)
                .addOnSuccessListener { documentReference ->

                     print("adding currentUser")

                }.addOnFailureListener { ex ->

                }

    }
}