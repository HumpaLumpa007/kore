package com.typingsolutions.kore.setup;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.typingsolutions.kore.R;


public class ExtendSetupFragment extends Fragment {

  private TextInputEditText mEditTextAsEnterPassword;
  private TextInputEditText mEditTextAsRepeatPassword;
  private TextInputEditText mEditTextAsEnterPIM;
  private TextInputEditText mEditTextAsRepeatPIM;
  private TextView mTextViewAsPIMHint;

  private String mBackupText;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.setup_fragment_3, container, false);
    mEditTextAsEnterPassword = (TextInputEditText) root.findViewById(R.id.setuplayout_edittext_passwordenter);
    mEditTextAsRepeatPassword = (TextInputEditText) root.findViewById(R.id.setuplayout_edittext_passwordrepeat);

    mEditTextAsEnterPIM = (TextInputEditText) root.findViewById(R.id.setuplayout_edittext_pimenterextended);
    mEditTextAsRepeatPIM = (TextInputEditText) root.findViewById(R.id.setuplayout_edittext_pimrepeatextended);

    mTextViewAsPIMHint = (TextView) root.findViewById(R.id.setuplayout_textview_currentpim);

    mEditTextAsEnterPassword.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        setCurrentPIM();
      }
    });

    return root;
  }

  private void setCurrentPIM() {
    if(mBackupText == null) {
      mBackupText = mTextViewAsPIMHint.getText().toString();
    }

    SetupActivity activity = (SetupActivity) getActivity();

    int calcPim = activity.calcPim(mEditTextAsEnterPassword.getText().toString());
    mTextViewAsPIMHint.setText(mBackupText.replace("${_pim_}", Integer.toString(calcPim)));
  }

  void setEnteredPasswords(CharSequence pw1, CharSequence pw2) {
    mEditTextAsEnterPassword.setText(pw1);
    mEditTextAsRepeatPassword.setText(pw2);
  }

  CharSequence getPassword1() {
    return mEditTextAsEnterPassword.getText();
  }

  CharSequence getPassword2() {
    return mEditTextAsRepeatPassword.getText();
  }
}
