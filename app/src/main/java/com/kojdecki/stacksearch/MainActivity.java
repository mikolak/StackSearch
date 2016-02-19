package com.kojdecki.stacksearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.kojdecki.stacksearch.views.Snackbar;


public class MainActivity extends Activity implements DetailsFragment.OnFragmentInteractionListener,
        SearchFragment.OnItemSelecetedListener {

    //private static final String TAG = "MainActivity:";

    private String SEARCH_FRAGMENT_KEY = "mSearchFragment";
    private String DETAILS_FRAGMENT_KEY = "mDetailsFragment";
    private String EXIT_DIALOG_KEY = "exitDialog";

    private SearchFragment mSearchFragment = null;
    private DetailsFragment mDetailsFragment = null;

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
            //transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Snackbar.make(this, getResources().getString(R.string.welcome),
                getResources().getString(R.string.hide), Snackbar.LENGTH_SHORT, null)
                .show();
//        Snackbar.make(this, getResources().getString(R.string.welcome),
//                getResources().getString(R.string.hide), Snackbar.LENGTH_SHORT, null)
//                .show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //not needed
    }

    @Override
    public void onBackPressed() {
        if (mDetailsFragment == null || !mDetailsFragment.isVisible()) {
            ExitDialogFragment exitDialogFragment = new ExitDialogFragment();
            exitDialogFragment.show(getFragmentManager(), EXIT_DIALOG_KEY);
        }
        else
            getFragmentManager().popBackStack();
        //super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getFragmentManager().putFragment(outState, SEARCH_FRAGMENT_KEY, mSearchFragment);
        if (mDetailsFragment != null)
            getFragmentManager().putFragment(outState, DETAILS_FRAGMENT_KEY, mDetailsFragment);
    }

    @Override
    public void onItemSelected(String link) {
        mDetailsFragment = DetailsFragment.newInstance(link);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public static class ExitDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.exit_question)
                    .setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
            return builder.create();
        }
    }
}
