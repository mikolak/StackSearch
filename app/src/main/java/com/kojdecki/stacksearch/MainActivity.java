package com.kojdecki.stacksearch;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;


public class MainActivity extends Activity implements DetailsFragment.OnFragmentInteractionListener,
        SearchFragment.OnFragmentInteractionListener {

    //private static final String TAG = "MainActivity:";

    private String SEARCH_FRAGMENT_KEY = "mSearchFragment";
    private String DETAILS_FRAGMENT_KEY = "mDetailsFragment";

    private SearchFragment mSearchFragment = null;
    private DetailsFragment mDetailsFragment = null;

    //private ActiveFragment mActiveFragment = ActiveFragment.SEARCH;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSearchFragment = (SearchFragment) getFragmentManager()
                    .getFragment(savedInstanceState, SEARCH_FRAGMENT_KEY);
            mDetailsFragment = (DetailsFragment) getFragmentManager()
                    .getFragment(savedInstanceState, DETAILS_FRAGMENT_KEY);
        }

        if (mSearchFragment == null) {
            mSearchFragment = SearchFragment.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, mSearchFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //TODO implement this
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, SEARCH_FRAGMENT_KEY, mSearchFragment);
        if (mDetailsFragment != null)
            getFragmentManager().putFragment(outState, DETAILS_FRAGMENT_KEY, mDetailsFragment);
    }


    /*private enum ActiveFragment {
        SEARCH,
        DETAILS
    }*/
}
