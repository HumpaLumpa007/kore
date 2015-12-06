package com.typingsolutions.passwordmanager.callbacks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import com.typingsolutions.passwordmanager.activities.LoginActivity;
import com.typingsolutions.passwordmanager.activities.PasswordOverviewActivity;
import core.DatabaseProvider;
import core.exceptions.LoginException;

public class LoginCallback extends BaseCallback {
  private LoginActivity loginActivity;
  private String password;
  private boolean safeLogin;
  private ProgressDialog progress;

  private final DatabaseProvider.OnOpenListener openListener = new DatabaseProvider.OnOpenListener() {
    @Override
    public void open() {
      Intent intent = new Intent(context, PasswordOverviewActivity.class);
      intent.putExtra(LoginActivity.SAFELOGIN, safeLogin);
      context.startActivity(intent);
      loginActivity.finish();
    }

    @Override
    public void refused() {
      try {
        Snackbar.make(loginActivity.getRootView(), "Your password is wrong!", Snackbar.LENGTH_LONG).show();
        loginActivity.getLoginServiceRemote().increaseTries();
      } catch (RemoteException e) {
        Snackbar.make(loginActivity.getRootView(), "Sorry, something went wrong", Snackbar.LENGTH_LONG).show();
      }
    }
  };


  public LoginCallback(Context context, LoginActivity activity) {
    super(context);
    this.loginActivity = activity;
  }

  @Override
  public void onClick(View v) {
    try {
      if (loginActivity.getLoginServiceRemote().isUserBlocked())
        throw new LoginException("Sorry, you're blocked", LoginException.BLOCKED);

      DatabaseProvider provider = DatabaseProvider.getConnection(context);
      AppCompatSpinner spinner = new AppCompatSpinner(context);
      provider.tryOpen(password, openListener);

    } catch (Exception e) {
      if (e instanceof LoginException
          && ((LoginException) e).getState() == LoginException.BLOCKED) {
        Snackbar.make(v, e.getMessage(), Snackbar.LENGTH_LONG).show();
      } else {
        Snackbar.make(v, "Sorry, something went wrong", Snackbar.LENGTH_LONG).show();
      }
    }
  }

  @Override
  public void setValues(Object... values) {
    if (values.length == 0) return;
    if (values[0] instanceof String) {
      password = (String) values[0];
    }
    if (values.length == 1) return;
    if (values[1] instanceof Boolean) {
      safeLogin = (boolean) values[1];
    }
  }
}
