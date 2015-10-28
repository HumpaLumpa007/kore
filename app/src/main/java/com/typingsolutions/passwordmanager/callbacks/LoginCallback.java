package com.typingsolutions.passwordmanager.callbacks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import com.typingsolutions.passwordmanager.activities.LoginActivity;
import com.typingsolutions.passwordmanager.activities.PasswordOverviewActivity;
import com.typingsolutions.passwordmanager.fragments.LoginPasswordFragment;
import core.data.User;
import core.data.UserProvider;
import core.exceptions.LoginException;

public class LoginCallback extends BaseCallback {
    private LoginActivity loginActivity;
    private String password;


    public LoginCallback(Context context, LoginActivity activity) {
        super(context);
        this.loginActivity = activity;
    }

    @Override
    public void onClick(View v) {
        User user = null;
        try {
            final SharedPreferences preferences = loginActivity.getPreferences(Context.MODE_PRIVATE);
            final boolean checked = preferences.getBoolean(LoginPasswordFragment.SAFELOGIN, false);
            user = UserProvider.getInstance(context).login(loginActivity.getLoginServiceRemote(), password, checked);

            Intent intent = new Intent(context, PasswordOverviewActivity.class);
            context.startActivity(intent);
        } catch (Exception e) {
            if (e instanceof LoginException) {
                LoginException exception = (LoginException) e;

                Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();

                Fragment fragment = loginActivity.getSupportFragmentManager().getFragments().get(0);

                if (fragment instanceof LoginPasswordFragment) {
                    LoginPasswordFragment loginPasswordFragment = (LoginPasswordFragment) fragment;

                    if (exception.getState() == LoginException.WRONG) {
                        loginPasswordFragment.retypePassword();
                    }
                }
            } else {
                Snackbar.make(v, "Sorry, something went wrong", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void setValues(Object... values) {
        if (values[0] instanceof String) {
            password = (String) values[0];
        }
    }
}
