package core.data;

import android.content.Context;
import android.support.annotation.Nullable;
import core.DatabaseProvider;
import core.exceptions.PasswordProviderException;
import core.exceptions.UserProviderException;

import java.util.*;

public class PasswordProvider {
  private static PasswordProvider Instance;

  private Context context;
  private List<Password> passwords;
  private PasswordActionListener passwordActionListener;
  private boolean safe;

  private PasswordProvider(Context context) {
    this.context = context;
    this.passwords = new ArrayList<>();
  }

  public static PasswordProvider getInstance(Context context) {
    if (Instance == null)
      Instance = new PasswordProvider(context);
    return Instance;
  }

  public int size() {
    return passwords.size();
  }

  public Password get(int index) {
    return passwords.get(index);
  }

  public Password getById(int id) {
    for (Password password : passwords) {
      if (password.getId() == id) {
        return password;
      }
    }
    return null;
  }

  public static void logoutComplete() {
    Instance.simpleLogout();
    Instance.context = null;
    Instance = null;
  }

  public static boolean isLoggedIn() {
    return Instance != null;
  }

  void simpleLogout() {
    for (Password p : passwords) {
      p.logout();
    }
    passwords.clear();
  }

  public void order(int which) {
    if (which == 0) {   // order by username ascending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getUsername(), lhs.getUsername(), false);
        }
      });
    } else if (which == 1) {    // order by username descending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getUsername(), lhs.getUsername(), true);
        }
      });
    } else if (which == 2) {   // order by password ascending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getFirstItem(), lhs.getFirstItem(), false);
        }
      });
    } else if (which == 3) {    // order by password descending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getFirstItem(), lhs.getFirstItem(), true);
        }
      });
    } else if (which == 4) {   // order by program ascending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getProgram(), lhs.getProgram(), false);
        }
      });
    } else if (which == 5) {    // order by program descending
      Collections.sort(passwords, new Comparator<Password>() {
        @Override
        public int compare(Password lhs, Password rhs) {
          return PasswordProvider.this.compare(rhs, lhs, rhs.getProgram(), lhs.getProgram(), true);
        }
      });
    }

    if(passwordActionListener != null)
      passwordActionListener.onOrder();
  }

  private int compare(Password rhs, Password lhs, String compareRhs, String compareLhs, boolean invert) {
    int compareTo = compareLhs.compareTo(compareRhs);
    if (compareTo != 0)
      lhs.swapPositionWith(rhs);

    compareTo = invert ? ~compareTo : compareTo;

    return compareTo;
  }

  public Password addPassword(String program, String username, String password) throws Exception {
    int position = passwords.size() + 1;

    DatabaseProvider provider = DatabaseProvider.getConnection(context);
    long passwordId = provider.insert(DatabaseProvider.INSERT_NEW_PASSWORD, username, program, position);
    if (passwordId < 0)
      throw new PasswordProviderException("Couldn't insert your password");

    long historyId = provider.insert(DatabaseProvider.INSERT_NEW_HISTORY, password, passwordId);
    if (historyId < 0)
      throw new PasswordProviderException("Couldn't insert your password!");

    Password passwordObject = new Password((int) passwordId, position, username, program);
    passwordObject.addPasswordHistoryItem((int) historyId, PasswordHistory.createItem(password));

    return addPassword(passwordObject);
  }

  public Password addPassword(Password password) throws Exception {

    password.reversePasswordHistory();

    if (!passwords.contains(password))
      passwords.add(password);

    if (passwordActionListener != null)
      passwordActionListener.onPasswordAdded(password);

    return password;
  }

  public void editPassword(int id, String newPassword) throws Exception {
    DatabaseProvider provider = DatabaseProvider.getConnection(context);

    PasswordHistory history = PasswordHistory.createItem(newPassword);

    long historyId = provider.insert(DatabaseProvider.INSERT_NEW_HISTORY, newPassword, id);

    if (historyId == -1)
      throw new UserProviderException("Couldn't insert your password history item!");

    Password password = getById(id);
    password.addPasswordHistoryItem((int) historyId, history);

    editPassword(password);
  }

  public void editPassword(int id, @Nullable String program, @Nullable String username) throws Exception {
    Password password = getById(id);
    password.setUsername(username);
    password.setProgram(program);

    DatabaseProvider.getConnection(context)
        .update(DatabaseProvider.UPDATE_PASSWORD_BY_ID, program, username, id);

    editPassword(password);
  }

  void editPassword(Password password) throws Exception {
    int index = passwords.indexOf(password);
    passwords.set(index, password);

    if (passwordActionListener != null)
      passwordActionListener.onPasswordEdited(password, password.getFirstHistoryItem());
  }

  public Password removePassword(int position) {
    return removePassword(get(position));
  }

  public Password removePassword(Password password) {
    DatabaseProvider provider = DatabaseProvider.getConnection(context);

    List<Integer> list = new ArrayList<>(password.getPasswordIds());
    for (Integer i : list) {
      provider.remove(DatabaseProvider.REMOVE_HISTORY_BY_ID, i);
    }

    provider.remove(DatabaseProvider.REMOVE_PASSWORD_BY_ID, password.getId());

    passwords.remove(password);

    if (passwordActionListener != null)
      passwordActionListener.onPasswordRemoved(password);

    return password;
  }

  public void swapPassword(int from, int to) {
    Collections.swap(passwords, from, to);
    Password fromPassword = PasswordProvider.getInstance(context).get(from);
    Password toPassword = PasswordProvider.getInstance(context).get(to);
    fromPassword.swapPositionWith(toPassword);
  }

  public void setPasswordActionListener(PasswordActionListener passwordActionListener) {
    this.passwordActionListener = passwordActionListener;
  }

  public boolean isSafe() {
    return this.safe;
  }

  public void isSafe(boolean safe) {
    this.safe = safe;
  }

  public interface PasswordActionListener {
    void onPasswordAdded(Password password);

    void onPasswordRemoved(Password password);

    void onPasswordEdited(Password password, PasswordHistory history);

    void onOrder();
  }
}
