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

    private final String SEARCH_FRAGMENT_KEY = "mSearchFragment";
    private final String DETAILS_FRAGMENT_KEY = "mDetailsFragment";
    private final String EXIT_DIALOG_KEY = "exitDialog";
    private final String SNACKBARS_SHOWN_KEY = "mSnackbarsShown";

    private SearchFragment mSearchFragment = null;
    private DetailsFragment mDetailsFragment = null;

    private boolean mSnackbarsShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mSearchFragment = (SearchFragment) getFragmentManager()
                    .getFragment(savedInstanceState, SEARCH_FRAGMENT_KEY);
            mDetailsFragment = (DetailsFragment) getFragmentManager()
                    .getFragment(savedInstanceState, DETAILS_FRAGMENT_KEY);

            mSnackbarsShown = savedInstanceState.getBoolean(SNACKBARS_SHOWN_KEY, false);
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
        if (!mSnackbarsShown) {
            Snackbar.make(this, getResources().getString(R.string.welcome),
                    getResources().getString(R.string.hide), Snackbar.LENGTH_SHORT, null)
                    .show();
            Snackbar.make(this, getResources().getString(R.string.welcome),
                    getResources().getString(R.string.hide), Snackbar.LENGTH_LONG, null)
                    .show();
            Snackbar.make(this, getResources().getString(R.string.welcome),
                    getResources().getString(R.string.hide), Snackbar.LENGTH_INDEFINITE, null)
                    .show();
            mSnackbarsShown = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Snackbar.cancel();
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
        if (mDetailsFragment != null && mDetailsFragment.isVisible())
            getFragmentManager().putFragment(outState, DETAILS_FRAGMENT_KEY, mDetailsFragment);

        outState.putBoolean(SNACKBARS_SHOWN_KEY, mSnackbarsShown);
    }

    @Override
    public void onItemSelected(String link) {
        if (link == null || link.equals(""))
            return;
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
