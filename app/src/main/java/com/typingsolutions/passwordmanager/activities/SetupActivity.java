package com.typingsolutions.passwordmanager.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.typingsolutions.passwordmanager.BaseDatabaseActivity;
import com.typingsolutions.passwordmanager.BaseFragment;
import com.typingsolutions.passwordmanager.R;
import com.typingsolutions.passwordmanager.fragments.BottomSheetViewerFragment;
import com.typingsolutions.passwordmanager.adapter.SetupPagerAdapter;
import com.typingsolutions.passwordmanager.database.DatabaseConnection;
import com.typingsolutions.passwordmanager.fragments.SetupPasswordFragment;
import com.typingsolutions.passwordmanager.fragments.SimpleViewFragment;
import net.sqlcipher.database.SQLiteDatabase;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class SetupActivity extends BaseDatabaseActivity {
  private CoordinatorLayout mCoordinatorLayoutAsRootLayout;
  private ui.ViewPager mViewPagerAsFragmentHost;
  private ImageButton mImageButtonAsNext;

  private SetupPagerAdapter mSetupPagerAdapter;

  private final int[] imageResources = {R.drawable.android, R.mipmap.security, R.mipmap.pim, R.mipmap.setup_done};

  private final int[] mHelpLayouts = {R.layout.setup_1_help_layout, R.layout.setup_2_help_layout};

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.setup_main_layout);
    logout = false;

    mCoordinatorLayoutAsRootLayout = findCastedViewById(R.id.setuplayout_coordinatorlayout_root);
    mViewPagerAsFragmentHost = findCastedViewById(R.id.setuplayout_viewpager_content);
    mImageButtonAsNext = findCastedViewById(R.id.setuplayout_button_next);

    setStatusBarColor(0x44000000);

    FloatingActionButton fab = findCastedViewById(R.id.setuplayout_fab_expandBottom);
    fab.setOnClickListener(v -> toggleHelp());

    mSetupPagerAdapter = new SetupPagerAdapter(getSupportFragmentManager(),
        new BaseFragment[]{
            SimpleViewFragment.create(R.layout.setup_1_content_layout),
            new SetupPasswordFragment()
        });


    mViewPagerAsFragmentHost.setAdapter(mSetupPagerAdapter);
    mViewPagerAsFragmentHost.setOffscreenPageLimit(1);
    mViewPagerAsFragmentHost.canSwipe(false);

    mImageButtonAsNext.setOnClickListener(v -> next());
  }

  private void toggleHelp() {
    BottomSheetDialogFragment fragment = BottomSheetViewerFragment.create(mHelpLayouts[mViewPagerAsFragmentHost.getCurrentItem()]);
    fragment.show(getSupportFragmentManager(), fragment.getTag());
  }

  @Override
  protected View getSnackbarRelatedView() {
    return mCoordinatorLayoutAsRootLayout;
  }

  @Override
  protected void onActivityChange() {

  }

  public void next() {
    int index = mViewPagerAsFragmentHost.getCurrentItem();
    if (index == 0) {
      mViewPagerAsFragmentHost.setCurrentItem(index + 1, true);
    } else if (index == 1) {
      SetupPasswordFragment item = (SetupPasswordFragment) mSetupPagerAdapter.getItem(index);

      item.getEnteredPassword();
    }
  }

  public void setupDatabase(String password) {
    try {
      MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
      shaDigest.update(password.getBytes());

      BigInteger integer = new BigInteger(shaDigest.digest());
      Random r = new Random(integer.longValue());
      int pim = r.nextInt(1100) + 485;
      Log.d(getClass().getSimpleName(), "SETUPPIM: " + pim);

      shaDigest.reset();

      DatabaseConnection connection = new DatabaseConnection(getApplicationContext(), password, pim);
      SQLiteDatabase database = connection.getWritableDatabase(password);
      if (!database.isOpen()) {
        makeSnackbar("Couldn't create database");
      }
      database.close();
    } catch (NoSuchAlgorithmException e) {
      makeSnackbar("Couldn't create database");
    }
  }

  public void setPassword(String password) {
    if(password == null) return;

    setupDatabase(password);

    startActivity(PasswordOverviewActivity.class, true);
  }
}
