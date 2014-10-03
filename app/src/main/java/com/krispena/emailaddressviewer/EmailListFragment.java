package com.krispena.emailaddressviewer;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class EmailListFragment extends ListFragment {

    public static final int MESSAGE_SUCCESS = 1;
    public static final int MESSAGE_FAIL = 2;
    private static final int LOADER_ID = 1;
    private static final int ADD_MENU_ID = 1;

    private static final String STATE_INDEX_EXTRA = "StateIndex";
    private static final String STATE_TOP_EXTRA = "StateTop";

    /* Handler to forward UI updates and success/fail messages */
    private Handler handler;
    private CursorAdapter adapter;


    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
            // query ContentProvider for all data and return CursorLoader
            return new CursorLoader(getActivity(), EmailProvider.CONTENT_URI, null, null, null, null);

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            adapter.swapCursor(data); // update adapter
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            adapter.swapCursor(null); // cleanup adapter
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == MESSAGE_FAIL) { //encountered duplicate email address
                    showDuplicateEmailAlertDialog();
                }
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // restore state
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            int top = savedInstanceState.getInt(STATE_TOP_EXTRA);
            int index = savedInstanceState.getInt(STATE_INDEX_EXTRA);
            getListView().setSelectionFromTop(index, top);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initLoader();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (getListView() != null) { // this should not be null here, but it's safe to check
            // save to bundle
            ListPosition position = getListPosition();
            outState.putInt(STATE_INDEX_EXTRA, position.index);
            outState.putInt(STATE_TOP_EXTRA, position.top);
        }
    }

    private ListPosition getListPosition() {
        int index = getListView().getFirstVisiblePosition();
        View view = getListView().getChildAt(0);
        int top = (view == null) ? 0 : view.getTop();
        return new ListPosition(index,top);
    }

    /**
     * Initialize Loader and setup LoaderCallback methods
     */
    private void initLoader() {

        getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
    }

    /**
     * Initialize list adapter
     */
    private void initAdapter() {
        adapter = new SimpleCursorAdapter(getActivity(),
                R.layout.list_item, null,
                new String[]{Email.EMAIL_ADDRESS},
                new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);
    }

    private void showDuplicateEmailAlertDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.duplicate_email_title)
                .setMessage(R.string.duplicate_email_message).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //allow dialog to cancel
            }
        }).show();
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        TextView textView = (TextView) view;
        String emailAddress = textView.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", emailAddress, null));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(Menu.NONE, ADD_MENU_ID, Menu.NONE, R.string.menu_item_add).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == ADD_MENU_ID) {
            // cleanup dialog fragment
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            Fragment prev = getFragmentManager().findFragmentByTag("dialog");
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);

            // Create and show the dialog.
            DialogFragment newFragment = AddEmailDialogFragment.newInstance(new AddEmailDialogFragment.OnEmailEnteredListener() {
                @Override
                public void onEmailEntered(String email) {
                    if (email != null && email.trim().length() > 0) {
                        InsertEmailTask task = new InsertEmailTask(getActivity().getContentResolver(), handler);
                        task.execute(email);
                    }
                }
            });
            newFragment.show(ft, "dialog");


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Internal POJO to cleanly pass list state
     */
    private class ListPosition {
        int top;
        int index;

        private ListPosition(int index, int top) {
            this.index = index;
            this.top = top;
        }
    }


}
