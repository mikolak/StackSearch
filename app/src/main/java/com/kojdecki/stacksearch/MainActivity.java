package com.kojdecki.stacksearch;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

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

    private static final String TAG = "MainActivity:";
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

        final SearchView searchView = (SearchView) findViewById(R.id.search_box);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
//                //Query doesn't work if it ends with a space
//                if (query.endsWith(" "))
//                    query = query.substring(0, query.length() - 1);
                mLastQuery = query;
                mPageNumber = 1;
                mQuestionListAdapter.clear();
                footerView = new ProgressBar(MainActivity.this);
                listView.addFooterView(footerView);
                searchStackOverflow(mPageNumber);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchStackOverflow(int page) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        StackOverflowAPI stackOverflowAPI = retrofit.create(StackOverflowAPI.class);
        mCall = stackOverflowAPI.loadSearch(mLastQuery.replace(" ", ","), page);
        mCall.enqueue(this);
        mCallRunning = true;
        return;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO implement this
    }

    @Override
    public void onResponse(Call<Search> call, Response<Search> response) {
        ListView listView = (ListView) findViewById(R.id.results_list);
        mQuestionListAdapter.addAll(response.body().getItems());
        if (mQuestionListAdapter.getCount() == 0)
            mQuestionListAdapter.add(new Question(-1, new ShallowUser(null, null), "No hits for this search!"));
        mCallRunning = false;
        Log.d(TAG + ":onRespon", response.body().toString());
        listView.removeFooterView(footerView);
    }

    @Override
    public void onFailure(Call<Search> call, Throwable t) {
        mCallRunning = false;
        //TODO snackbar with retry
        ListView listView = (ListView) findViewById(R.id.results_list);
        listView.removeFooterView(footerView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO check internet connection and display a dialog if need be (ONCE, we don't wanna pest the user)
        if(mLastQuery != null && mCallRunning) {
            searchStackOverflow(mPageNumber);
            //TODO set refeshing icon in the ListView
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
        ListView listView = (ListView) findViewById(R.id.results_list);
        mQuestionListAdapter
                .addAll((ArrayList<Question>) savedInstanceState.getSerializable("AdapterItems"));
    }

    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
