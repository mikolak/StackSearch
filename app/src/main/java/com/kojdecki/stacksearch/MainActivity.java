package com.kojdecki.stacksearch;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.kojdecki.stacksearch.retrofit.Question;
import com.kojdecki.stacksearch.retrofit.Search;
import com.kojdecki.stacksearch.retrofit.ShallowUser;
import com.kojdecki.stacksearch.retrofit.StackOverflowAPI;
import com.kojdecki.stacksearch.views.QuestionListAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends Activity implements Callback<Search>, DetailsFragment.OnFragmentInteractionListener {

    //private static final String TAG = "MainActivity:";
    private String mLastQuery = null;
    private Call<Search> mCall = null;
    private boolean mCallRunning = false;
    private short mPageNumber = 1;
    private View footerView = null;
    private QuestionListAdapter mQuestionListAdapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.results_list);
        mQuestionListAdapter = new QuestionListAdapter(new ArrayList<Question>(), this);
        listView.setAdapter(mQuestionListAdapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //not needed
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount
                        && totalItemCount > 1
                        && mPageNumber > 1
                        && !mCallRunning) {
                    searchStackOverflow(mPageNumber);
                }
            }
        });

        final SearchView searchView = (SearchView) findViewById(R.id.search_box);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                mLastQuery = query;
                mPageNumber = 1;
                mQuestionListAdapter.clear();

                if (isNetworkConnected())
                    searchStackOverflow(mPageNumber);
                else {
                    Toast.makeText(MainActivity.this, "No network connection", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainActivity.this.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    private void searchStackOverflow(int page) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ListView listView = (ListView) findViewById(R.id.results_list);
        footerView = new ProgressBar(MainActivity.this);
        listView.addFooterView(footerView);

        StackOverflowAPI stackOverflowAPI = retrofit.create(StackOverflowAPI.class);
        mCall = stackOverflowAPI.loadSearch(mLastQuery.replace(" ", ","), page);
        mCall.enqueue(this);
        mCallRunning = true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO implement this
    }

    @Override
    public void onResponse(Call<Search> call, Response<Search> response) {
        ListView listView = (ListView) findViewById(R.id.results_list);
        mQuestionListAdapter.addAll(response.body().getItems());
        if (mQuestionListAdapter.getCount() == 0) {
            mQuestionListAdapter.add(new Question(-1, new ShallowUser(null, null), "No hits for this search!"));
            mPageNumber = 1;
        } else {
            mPageNumber += 1;
        }
        mCallRunning = false;
        listView.removeFooterView(footerView);
    }

    @Override
    public void onFailure(Call<Search> call, Throwable t) {
        mCallRunning = false;
        Toast.makeText(this, "Application failed to fetch results", Toast.LENGTH_SHORT).show();
        ListView listView = (ListView) findViewById(R.id.results_list);
        listView.removeFooterView(footerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mLastQuery != null && mCallRunning) {
            searchStackOverflow(mPageNumber);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mCallRunning", mCallRunning);
        outState.putString("mLastQuery", mLastQuery);
        outState.putShort("mPageNumber", mPageNumber);
        outState.putSerializable("AdapterItems",
                mQuestionListAdapter.getItems());
    }

    @Override
    protected void onPause() {
        if (mCallRunning) {
            mCall.cancel();
        }
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCallRunning = savedInstanceState.getBoolean("mCallRunning", false);
        mLastQuery = savedInstanceState.getString("mLastQuery", null);
        mPageNumber = savedInstanceState.getShort("mPageNumber", (short) 1);
        mQuestionListAdapter
                .addAll((ArrayList<Question>) savedInstanceState.getSerializable("AdapterItems"));
    }
}
