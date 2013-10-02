package com.peppeuz.googlep;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends Activity implements OnClickListener,
		ConnectionCallbacks, OnConnectionFailedListener, OnPersonLoadedListener {
	private static final String TAG = "ExampleActivity";
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

	private ProgressDialog mConnectionProgressDialog;
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	Button logout;
	Button share;
	SignInButton signin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		logout = (Button) findViewById(R.id.logout);
		share = (Button) findViewById(R.id.share_button);
		signin = (SignInButton) findViewById(R.id.sign_in_button);
		signin.setOnClickListener(this);
		share.setOnClickListener(this);
		logout.setOnClickListener(this);
		mPlusClient = new PlusClient.Builder(this, this, this)
				.setVisibleActivities("http://schemas.google.com/AddActivity",
						"http://schemas.google.com/BuyActivity").build();
		// Progress bar to be displayed if the connection failure is not
		// resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Signing in...");
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		if (result.hasResolution()) {
			try {
				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				mPlusClient.connect();
			}
		}
		// Salva il risultato e risolvi l'errore di connessione al momento in
		// cui un utente fa clic.
		mConnectionResult = result;
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR
				&& responseCode == RESULT_OK) {
			mConnectionResult = null;
			mPlusClient.connect();
		}
	}

	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mConnectionProgressDialog.dismiss();
		Toast.makeText(this,
				"User is connected! " + mPlusClient.getAccountName(),
				Toast.LENGTH_LONG).show();
		mPlusClient.loadPerson(this, "me");
		signin.setVisibility(View.GONE);
		logout.setVisibility(View.VISIBLE);
		share.setVisibility(View.VISIBLE);
	}


	@Override
	public void onPersonLoaded(ConnectionResult status, Person person) {
		// TODO Auto-generated method stub

		if (status.getErrorCode() == ConnectionResult.SUCCESS) {
			Log.d(TAG, "Display Name: " + person.getDisplayName());
		}

	}
	
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.sign_in_button:
			mPlusClient.connect();
			if (!mPlusClient.isConnected()) {
				if (mConnectionResult == null) {
					mConnectionProgressDialog.show();
				} else {
					try {
						mConnectionResult.startResolutionForResult(this,
								REQUEST_CODE_RESOLVE_ERR);
					} catch (SendIntentException e) {
						// Riprova a connetterti.
						mConnectionResult = null;
						mPlusClient.connect();
					}
				}
				
			}
			break;
		case R.id.share_button:

			// Launch the Google+ share dialog with attribution to your app.
			Intent shareIntent = new PlusShare.Builder(this)
					.setType("text/plain")
					.setText("AndroidWorld - DevCorner")
					.setContentUrl(
							Uri.parse("http://www.androidworld.it/2013/09/17/devcorner-login-post-google-184703"))
					.getIntent();

			startActivityForResult(shareIntent, 0);
			break;

		case R.id.logout:
			// Logout
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
			}
			// Revoca Token

			logout.setVisibility(View.GONE);
			signin.setVisibility(View.VISIBLE);
			share.setVisibility(View.GONE);
			break;
		default:
			break;
		}

	}

}