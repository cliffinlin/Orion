package com.android.orion;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.LogInCallback;

public class LeanCloudLoginActivity extends OrionBaseActivity {

	protected EditText usernameEditText = null;
	protected EditText passwordEditText = null;

	protected Button loginButton = null;

	protected TextView signUpTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_login);

		usernameEditText = (EditText) findViewById(R.id.usernameField);
		passwordEditText = (EditText) findViewById(R.id.passwordField);
		loginButton = (Button) findViewById(R.id.loginButton);

		signUpTextView = (TextView) findViewById(R.id.signUpText);

		if (usernameEditText != null) {
			usernameEditText.setText(getSetting(
					Constants.SETTING_KEY_USER_NAME, ""));
		}

		signUpTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO
				// Intent intent = new Intent(LeanCloudLoginActivity.this,
				// LeanCloudSignUpActivity.class);
				// startActivity(intent);
			}
		});

		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();

				username = username.trim();
				password = password.trim();

				if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LeanCloudLoginActivity.this);
					builder.setMessage(R.string.login_error_message)
							.setTitle(R.string.login_error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					setProgressBarIndeterminateVisibility(true);

					saveSetting(Constants.SETTING_KEY_USER_NAME, username);

					AVUser.logInInBackground(username, password,
							new LogInCallback<AVUser>() {
								@Override
								public void done(AVUser user, AVException e) {
									setProgressBarIndeterminateVisibility(false);

									if (e == null) {
										// Success!
										LeanCloudLoginActivity.this
												.setResult(RESULT_OK);
										LeanCloudLoginActivity.this.finish();
									} else {
										// Fail
										AlertDialog.Builder builder = new AlertDialog.Builder(
												LeanCloudLoginActivity.this);
										builder.setMessage(e.getMessage())
												.setTitle(
														R.string.login_error_title)
												.setPositiveButton(
														android.R.string.ok,
														null);
										AlertDialog dialog = builder.create();
										dialog.show();
									}
								}
							});
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.action_settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
