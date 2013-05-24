package com.marakana.android.contactdialer;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.util.Log;

public class ContactListActivity extends Activity {
	private static final String TAG = "ContactListActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.contact_list_activity);

		StrictMode.setThreadPolicy(new ThreadPolicy.Builder().detectAll()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());
	}
}
