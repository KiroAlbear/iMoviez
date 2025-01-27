/*
 *  Copyright 2018 Soojeong Shin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.popularmovies.data;

import androidx.paging.PageKeyedDataSource;
import androidx.annotation.NonNull;

import android.util.Log;

import com.example.android.popularmovies.model.FireBaseModel.FirebaseMovieModel;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.MovieResponse;
import com.example.android.popularmovies.utilities.Constant;
import com.example.android.popularmovies.utilities.Controller;
import com.example.android.popularmovies.utilities.TheMovieApi;
import com.example.android.popularmovies.viewModels.FireBaseViewModel;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.android.popularmovies.utilities.Constant.NEXT_PAGE_KEY_TWO;
import static com.example.android.popularmovies.utilities.Constant.PREVIOUS_PAGE_KEY_ONE;
import static com.example.android.popularmovies.utilities.Constant.RESPONSE_CODE_API_STATUS;

/**
 * The MovieDataSource is the base class for loading snapshots of movie data into a given PagedList,
 * which is backed by the network. Since the TMDb API includes a key with each page load, extend
 * from PageKeyedDataSource.
 * <p>
 * Reference: @see "https://proandroiddev.com/8-steps-to-implement-paging-library-in-android-d02500f7fffe"
 * "https://www.youtube.com/watch?v=Ts-uxYiBEQ8" "https://www.youtube.com/watch?v=QVMqCRs0BNA"
 * "https://codelabs.developers.google.com/codelabs/android-paging/index.html#2"
 */
public class MovieDataSource extends PageKeyedDataSource<Integer, Movie> {

    /**
     * Tag for logging
     */
    private static final String TAG = MovieDataSource.class.getSimpleName();

    /**
     * Member variable for TheMovieApi interface
     */
    private TheMovieApi mTheMovieApi;

    /**
     * String for the sort order of the movies
     */
    private String mSortCriteria;

    private FireBaseViewModel firebaseVM = new FireBaseViewModel(null);

    public MovieDataSource(String sortCriteria) {
        mTheMovieApi = Controller.getClient().create(TheMovieApi.class);
        mSortCriteria = sortCriteria;
    }

    /**
     * This method is called first to initialize a PageList with data.
     */
    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params,
                            @NonNull final LoadInitialCallback<Integer, Movie> callback) {
        if (!mSortCriteria.equals("search")) {
            mTheMovieApi.getMovies(mSortCriteria, Constant.API_KEY, Constant.LANGUAGE, Constant.PAGE_ONE)
                    .enqueue(new Callback<MovieResponse>() {
                        @Override
                        public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                            if (response.isSuccessful()) {
                                List<Movie> res = response.body().getMovieResults();
                                res = removeOutlires(res);
                                callback.onResult(res,
                                        PREVIOUS_PAGE_KEY_ONE, NEXT_PAGE_KEY_TWO);

                            } else if (response.code() == RESPONSE_CODE_API_STATUS) {
                                Log.e(TAG, "Invalid Api key. Response code: " + response.code());
                            } else {
                                Log.e(TAG, "Response Code: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<MovieResponse> call, Throwable t) {
                            Log.e(TAG, "Failed initializing a PageList: " + t.getMessage());
                        }
                    });
        } else {
            mTheMovieApi.searchMovie(Constant.API_KEY, Constant.LANGUAGE, 1, Constant.SEARCH_KEYWORD).enqueue(new Callback<MovieResponse>() {
                @Override
                public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {

                    List<Movie> res = response.body().getMovieResults();
                    res=removeOutlires(res);
                    callback.onResult(res, PREVIOUS_PAGE_KEY_ONE, NEXT_PAGE_KEY_TWO);
                }

                @Override
                public void onFailure(Call<MovieResponse> call, Throwable t) {

                }
            });
        }
    }

    private List<Movie> removeOutlires(List<Movie> movieList) {
        //int listSize = movieList.size();
        for (int i = 0; i < movieList.size(); i++) {
            if (movieList.get(i).getPosterPath() == null || movieList.get(i).getBackdropPath() == null || movieList.get(i) == null)
            {
                movieList.remove(i);
                /// do not move forward
                if(i!=0)
                    i--;
            }
            else if(Integer.parseInt(getMovieYear(movieList.get(i).getReleaseDate())) >= 2000 && movieList.get(i).getVoteAverage()>=5)
                firebaseVM.getMovieByName(movieList.get(i).getTitle(),movieList.get(i).getReleaseDate(),Double.toString(movieList.get(i).getVoteAverage()));
        }
        return movieList;
    }
    private String getMovieYear(String releaseDate)
    {
        String[] temp = releaseDate.split("-");
        return temp[0];
    }

    /**
     * Prepend page with the key specified by LoadParams.key
     */
    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params,
                           @NonNull LoadCallback<Integer, Movie> callback) {

    }

    /**
     * Append page with the key specified by LoadParams.key
     */
    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params,
                          @NonNull final LoadCallback<Integer, Movie> callback) {

        final int currentPage = params.key;

        mTheMovieApi.getMovies(mSortCriteria, Constant.API_KEY, Constant.LANGUAGE, currentPage)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                        if (response.isSuccessful()) {
                            int nextKey = currentPage + 1;
                            List<Movie> res = response.body().getMovieResults();
                            removeOutlires(res);
                            callback.onResult(response.body().getMovieResults(), nextKey);
                        }
                    }

                    @Override
                    public void onFailure(Call<MovieResponse> call, Throwable t) {
                        Log.e(TAG, "Failed appending page: " + t.getMessage());
                    }
                });

    }
}
