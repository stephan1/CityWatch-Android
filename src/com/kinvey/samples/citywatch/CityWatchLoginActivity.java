/** 
 * Copyright (c) 2014 Kinvey Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.kinvey.samples.citywatch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.facebook.Session;
import com.facebook.SessionState;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

/**
 * @author mjsalinger
 * @since 2.0
 */
public class CityWatchLoginActivity extends SherlockActivity {

	public static final String TAG = CityWatchApplication.TAG;

	/**
	 * Kinvey Client
	 */
	private Client kinveyClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		kinveyClient = ((CityWatchApplication) getApplication()).getClient();

		// The FB SDK has a bit of a delay in response
		final ProgressDialog progressDialog = ProgressDialog.show(CityWatchLoginActivity.this, "Connecting to Facebook",
				"Logging in with Facebook - just a moment");

		doFacebookSso(progressDialog);

	}

	/**
	 * Facebook SSO Oauth
	 */
	private void doFacebookSso(final ProgressDialog progressDialog) {
		try {
			Session.openActiveSession(this, true, new Session.StatusCallback() {
				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (exception == null) {
						if (state.equals(RESULT_CANCELED)) {
							Toast.makeText(CityWatchLoginActivity.this, "FB login cancelled", Toast.LENGTH_LONG).show();
						} else if (state.isOpened()) {
							if (progressDialog != null && progressDialog.isShowing()) {
								progressDialog.dismiss();
							}
							Toast.makeText(CityWatchLoginActivity.this, "Logged in with Facebook.", Toast.LENGTH_LONG).show();

							loginFacebookKinveyUser(progressDialog, session.getAccessToken());
						}
					} else {
						error(progressDialog, exception.getMessage());
					}
				}
			});
		} catch (Exception ex) {
			Log.i(TAG, ex.getMessage());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	/*
	 * Login a Kinvey User with Faceook credentials
	 */
	private void loginFacebookKinveyUser(final ProgressDialog progressDialog, String accessToken) {

		kinveyClient.user().loginFacebook(accessToken, new KinveyUserCallback() {

			@Override
			public void onFailure(Throwable e) {
				CharSequence text = "Wrong username or password";
				Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}

			@Override
			public void onSuccess(User u) {
				progressDialog.dismiss();
				CharSequence text = "Logged in.";
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
				CityWatchLoginActivity.this.startActivity(new Intent(CityWatchLoginActivity.this, CityWatch.class));
				CityWatchLoginActivity.this.finish();
			}
		});

	}

	protected void error(ProgressDialog progressDialog, String error) {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		Log.d(TAG, "Error " + error);
	}
}
