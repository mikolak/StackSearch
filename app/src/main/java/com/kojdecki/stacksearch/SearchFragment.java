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
//    // TO DO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TO DO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

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

    // TO DO: Rename and change types and number of parameters
    public static SearchFragment newInstance(/*String param1, String param2*/) {
        SearchFragment fragment = new SearchFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        if (savedInstanceState != null) {
            mCallRunning = savedInstanceState.getBoolean("mCallRunning", false);
            mLastQuery = savedInstanceState.getString("mLastQuery", null);
            mPageNumber = savedInstanceState.getShort("mPageNumber", (short) 1);
            mQuestionList = (ArrayList<Question>) savedInstanceState.getSerializable("AdapterItems");
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
                    Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_SHORT).show();
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
            mQuestionListAdapter.add(new Question(-1, new ShallowUser(null, null), "No hits for this search!", ""));
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
        Toast.makeText(getActivity(), "Application failed to fetch results", Toast.LENGTH_SHORT).show();
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
        outState.putBoolean("mCallRunning", mCallRunning);
        outState.putString("mLastQuery", mLastQuery);
        outState.putShort("mPageNumber", mPageNumber);
        outState.putSerializable("AdapterItems",
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
        // Inflate the layout for this fragment
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemSelecetedListener {
        void onItemSelected(String link);
    }
}