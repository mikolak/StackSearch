package com.kojdecki.stacksearch;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
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


public class SearchFragment extends Fragment implements Callback<Search> {
    private final static String CALL_RUNNING_KEY = "mCallRunning";
    private final static String LAST_QUERY_KEY = "mLastQuery";
    private final static String PAGE_NUMBER_KEY = "mPageNumber";
    private final static String QUESTION_LIST_KEY = "mQuestionList";

    private String mLastQuery = null;
    private Call<Search> mCall = null;
    private boolean mCallRunning = false;
    private short mPageNumber = 1;
    private View footerView = null;
    private QuestionListAdapter mQuestionListAdapter = null;
    private ArrayList<Question> mQuestionList = null;
    private OnItemSelecetedListener mListener;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCallRunning = savedInstanceState.getBoolean(CALL_RUNNING_KEY, false);
            mLastQuery = savedInstanceState.getString(LAST_QUERY_KEY, null);
            mPageNumber = savedInstanceState.getShort(PAGE_NUMBER_KEY, (short) 1);
            mQuestionList = (ArrayList<Question>) savedInstanceState.getSerializable(QUESTION_LIST_KEY);
        } else {
            mQuestionList = new ArrayList<Question>();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = (ListView) getActivity().findViewById(R.id.results_list);
        mQuestionListAdapter = new QuestionListAdapter(mQuestionList, getActivity());
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onItemSelected(mQuestionListAdapter.getItem(position).getLink());
            }
        });

        final SearchView searchView = (SearchView) getActivity().findViewById(R.id.search_box);
        searchView.setSubmitButtonEnabled(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                mLastQuery = query;
                mPageNumber = 1;
                mQuestionListAdapter.clear();

                if (isNetworkConnected()) {
                    searchStackOverflow(mPageNumber);
                } else {
                    Toast.makeText(getActivity(), R.string.no_network, Toast.LENGTH_SHORT).show();
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
        ConnectivityManager cm = (ConnectivityManager) getActivity().
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

        ListView listView = (ListView) getActivity().findViewById(R.id.results_list);
        footerView = new ProgressBar(getActivity());
        listView.addFooterView(footerView);

        StackOverflowAPI stackOverflowAPI = retrofit.create(StackOverflowAPI.class);
        mCall = stackOverflowAPI.loadSearch(mLastQuery.replace(" ", ","), page);
        mCall.enqueue(this);
        mCallRunning = true;
    }

    @Override
    public void onResponse(Call<Search> call, Response<Search> response) {
        ListView listView = (ListView) getActivity().findViewById(R.id.results_list);
        mQuestionListAdapter.addAll(response.body().getItems());
        if (mQuestionListAdapter.getCount() == 0) {
            mQuestionListAdapter.add(new Question(-1, new ShallowUser(null, null), getResources().getString(R.string.no_hits), ""));
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
        Toast.makeText(getActivity(), R.string.failed_to_fetch, Toast.LENGTH_SHORT).show();
        ListView listView = (ListView) getActivity().findViewById(R.id.results_list);
        listView.removeFooterView(footerView);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLastQuery != null && mCallRunning) {
            searchStackOverflow(mPageNumber);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(CALL_RUNNING_KEY, mCallRunning);
        outState.putString(LAST_QUERY_KEY, mLastQuery);
        outState.putShort(PAGE_NUMBER_KEY, mPageNumber);
        outState.putSerializable(QUESTION_LIST_KEY,
                mQuestionList);
    }

    @Override
    public void onPause() {
        if (mCallRunning) {
            mCall.cancel();
        }
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnItemSelecetedListener) {
            mListener = (OnItemSelecetedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //interface necessary for parent activity
    public interface OnItemSelecetedListener {
        void onItemSelected(String link);
    }
}