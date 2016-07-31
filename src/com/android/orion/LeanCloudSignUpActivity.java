package com.android.orion;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SignUpCallback;

public class LeanCloudSignUpActivity extends OrionBaseActivity {

	protected EditText usernameEditText;
	protected EditText passwordEditText;
	protected EditText emailEditText;
	protected Button signUpButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_sign_up);

		usernameEditText = (EditText) findViewById(R.id.usernameField);
		passwordEditText = (EditText) findViewById(R.id.passwordField);
		emailEditText = (EditText) findViewById(R.id.emailField);
		signUpButton = (Button) findViewById(R.id.signupButton);

		signUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String username = usernameEditText.getText().toString();
				String password = passwordEditText.getText().toString();
				String email = emailEditText.getText().toString();

				username = username.trim();
				password = password.trim();
				email = email.trim();

				if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)
						|| TextUtils.isEmpty(email)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							LeanCloudSignUpActivity.this);
					builder.setMessage(R.string.signup_error_message)
							.setTitle(R.string.signup_error_title)
							.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				} else {
					setProgressBarIndeterminateVisibility(true);

					AVUser newUser = new AVUser();
					newUser.setUsername(username);
					newUser.setPassword(password);
					newUser.setEmail(email);
					newUser.signUpInBackground(new SignUpCallback() {
						@Override
						public void done(AVException e) {
							setProgressBarIndeterminateVisibility(false);

							if (e == null) {
								// Success!
								Intent intent = new Intent(
										LeanCloudSignUpActivity.this,
										OrionMainActivity.class);
								startActivity(intent);
							} else {
								AlertDialog.Builder builder = new AlertDialog.Builder(
										LeanCloudSignUpActivity.this);
								builder.setMessage(e.getMessage())
										.setTitle(R.string.signup_error_title)
										.setPositiveButton(android.R.string.ok,
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
		getMenuInflater().inflate(R.menu.sign_up, menu);
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
