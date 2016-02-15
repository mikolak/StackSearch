package com.kojdecki.stacksearch;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import com.kojdecki.stacksearch.retrofit.Question;
import com.kojdecki.stacksearch.retrofit.Search;
import com.kojdecki.stacksearch.retrofit.StackOverflowAPI;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends Activity implements Callback<Search>, DetailsFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity:";
    private String mLastQuery = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SearchView searchView = (SearchView) findViewById(R.id.search_box);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLastQuery = query;
                searchStackOverflow(1);
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
        Call<Search> call = stackOverflowAPI.loadSearch(mLastQuery.replace(" ", "%20"), page);
        call.enqueue(this);

        return;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO implement this
    }

    @Override
    public void onResponse(Call<Search> call, Response<Search> response) {
        //ArrayList<Question> list = new ArrayList<Question>();
        //list.addAll(response.body().getItems());
        Log.d(TAG + ":onRespon", response.body().toString());
    }

    @Override
    public void onFailure(Call<Search> call, Throwable t) {

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
