package com.example.android.popularmovies.Navigators

import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel

interface FireBaseNavigator {
    fun onMovieFounded(movie:FirebaseMovieModel?)
    fun onNotFound()
}