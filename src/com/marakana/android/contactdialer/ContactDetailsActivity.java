package com.marakana.android.contactdialer;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

public class ContactDetailsActivity extends Activity {
	private static final String TAG = "ContactDetailsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			super.finish();
		} else if (savedInstanceState == null) {
			ContactDetailsFragment contactDetailsFragment = new ContactDetailsFragment();
			contactDetailsFragment.setArguments(getIntent().getExtras());
			super.getFragmentManager().beginTransaction()
					.add(android.R.id.content, contactDetailsFragment).commit();
		}
	}
}
