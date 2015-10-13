package com.typingsolutions.passwordmanager.activities;

import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.*;
import android.animation.Animator;
import android.widget.TextView;
import com.typingsolutions.passwordmanager.R;
import com.typingsolutions.passwordmanager.ViewUtils;
import com.typingsolutions.passwordmanager.callbacks.AddPasswordCallback;
import core.*;
import core.adapter.PasswordOverviewAdapter;

public class PasswordOverviewActivity extends AppCompatActivity {

    private RecyclerView passwordRecyclerView;
    private Toolbar toolbar;
    private FloatingActionButton addPasswordFloatingActionButton;
    private TextView noPasswordsTextView;
    private SearchView searchView;

    private PasswordOverviewAdapter passwordOverviewAdapter;
    private AsyncPasswordLoader passwordLoader;
    private RecyclerView.LayoutManager layoutManager;

    private AsyncPasswordLoader.ItemAddCallback itemAddCallback = new AsyncPasswordLoader.ItemAddCallback() {
        @Override
        public void itemAdded(Password password) {
            int userId = UserProvider.getInstance(PasswordOverviewActivity.this).getId();
            PasswordProvider provider = PasswordProvider.getInstance(PasswordOverviewActivity.this, userId);

            if (!provider.contains(password)) {
                provider.add(password);
            }
        }
    };

    private PasswordProvider.OnPasswordAddedToDatabase onPasswordAddedToDatabase = new PasswordProvider.OnPasswordAddedToDatabase() {
        @Override
        public void onPasswordAdded(int passwordId, int historyId) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (noPasswordsTextView.getVisibility() == View.VISIBLE) {
                        noPasswordsTextView.setVisibility(View.INVISIBLE);
                    }

                    passwordOverviewAdapter.notifyDataSetChanged();
                }
            });

        }
    };

    private SearchView.OnCloseListener mOnCloseListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {
            passwordOverviewAdapter.resetFilter();
            // do not override default behaviour
            return false;
        }
    };

    private MenuItemCompat.OnActionExpandListener onSearchViewOpen = new MenuItemCompat.OnActionExpandListener() {
        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {


            return true;
        }

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {

            return true;
        }
    };

    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            passwordOverviewAdapter.filter(newText);
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_list_layout);

        // get elements from XML-View
        passwordRecyclerView = (RecyclerView) findViewById(R.id.passwordlistlayout_listview_passwords);
        toolbar = (Toolbar) findViewById(R.id.passwordlistlayout_toolbar);
        addPasswordFloatingActionButton = (FloatingActionButton) findViewById(R.id.passwordlistlayout_floatingactionbutton_add);
        noPasswordsTextView = (TextView) findViewById(R.id.passwordlistlayout_textview_nopasswords);

        // set onClick-event to add new passwords
        addPasswordFloatingActionButton.setOnClickListener(new AddPasswordCallback(this));

        // ...
        setSupportActionBar(toolbar);

        // get userId
        UserProvider userProvider = UserProvider.getInstance(this);
        int userId = userProvider.getId();

        // init and set adapter
        passwordOverviewAdapter = new PasswordOverviewAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        passwordRecyclerView.setAdapter(passwordOverviewAdapter);
        passwordRecyclerView.setLayoutManager(layoutManager);

        // init passwordProvider
        PasswordProvider provider = PasswordProvider.getInstance(PasswordOverviewActivity.this, userId);

        provider.setOnPasswordAddedToDatabase(onPasswordAddedToDatabase);

        // load passwords in background
        passwordLoader = new AsyncPasswordLoader(this, DatabaseProvider.GET_ALL_PASSWORDS_BY_USER_ID, Integer.toHexString(userId));
        passwordLoader.setItemAddCallback(itemAddCallback);
        passwordLoader.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.password_list_menu, menu);

        // init searchview
        MenuItem searchItem = menu.findItem(R.id.passwordlistmenu_item_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(onQueryTextListener);
        MenuItemCompat.setOnActionExpandListener(searchItem, onSearchViewOpen);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }
}
