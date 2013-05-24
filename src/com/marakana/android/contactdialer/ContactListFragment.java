package com.marakana.android.contactdialer;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ContactListFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = "ContactListFragment";

	public static interface OnContactSelectedListener {
		public void onContactSelected(Uri contactUri);
	}

	private static final String[] CONTACTS_SUMMARY_PROJECTION = new String[] {
			Contacts._ID, Contacts.DISPLAY_NAME };

	private static final String CONTACTS_SUMMARY_SELECTION = "(("
			+ Contacts.DISPLAY_NAME + " NOTNULL) AND (" + Contacts.DISPLAY_NAME
			+ " != '' ) AND (" + Contacts.HAS_PHONE_NUMBER + "=1))";

	private static final String CONTACTS_SUMMARY_SORT_ORDER = Contacts.DISPLAY_NAME
			+ " COLLATE LOCALIZED ASC";

	private static final String[] FROM_COLUMNS_NAMES = { Contacts.DISPLAY_NAME };

	private static final int[] TO_VIEW_IDS = { R.id.contact_name };

	private SimpleCursorAdapter simpleCursorAdapter;

	private boolean dualPane;

	private Uri currentContactUri = null;

	private int currentContactPosition = 0;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated(" + savedInstanceState + ")");
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			this.currentContactUri = savedInstanceState
					.getParcelable("currentContactUri");
			this.currentContactPosition = savedInstanceState
					.getInt("currentContactPosition");
		}
		View contactDetailsFrame = super.getActivity().findViewById(
				R.id.contact_details);
		this.dualPane = contactDetailsFrame != null
				&& contactDetailsFrame.getVisibility() == View.VISIBLE;
		super.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		Context context = super.getActivity().getApplicationContext();
		this.simpleCursorAdapter = new SimpleCursorAdapter(context,
				R.layout.contact_list_row, null, FROM_COLUMNS_NAMES,
				TO_VIEW_IDS, 0);

		super.setListAdapter(this.simpleCursorAdapter);
		super.getLoaderManager().initLoader(0, null, this);

		if (this.dualPane) {
			this.showDetails(this.currentContactUri,
					this.currentContactPosition);
		} else {
			ContactDetailsFragment contactDetailsFragment = (ContactDetailsFragment) super
					.getFragmentManager()
					.findFragmentById(R.id.contact_details);
			if (contactDetailsFragment != null) {
				super.getFragmentManager().beginTransaction()
						.remove(contactDetailsFragment).commit();
			}
		}
		Log.d(TAG, "onActivityCreated(...) ended with dualPane="
				+ this.dualPane);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState(...)");
		super.onSaveInstanceState(outState);
		outState.putParcelable("currentContactUri", this.currentContactUri);
		outState.putInt("currentContactPosition", this.currentContactPosition);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView(...)");
		return inflater.inflate(R.layout.contact_list_fragment, container,
				false);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(" + l + ", " + v + ", " + position + ", "
				+ id + ")");
		this.showDetails(ContentUris.withAppendedId(Contacts.CONTENT_URI, id),
				position);
	}

	private void showDetails(Uri currentContactUri, int currentContactPosition) {
		Log.d(TAG, "showDetails(" + currentContactUri + ", "
				+ currentContactPosition + ")");
		this.currentContactUri = currentContactUri;
		this.currentContactPosition = currentContactPosition;
		if (this.currentContactUri != null) {
			if (this.dualPane) {
				super.getListView().setItemChecked(this.currentContactPosition,
						true);
				ContactDetailsFragment contactDetailsFragment = (ContactDetailsFragment) super
						.getFragmentManager().findFragmentById(
								R.id.contact_details);
				if (contactDetailsFragment == null
						|| !currentContactUri.equals(contactDetailsFragment
								.getContactUri())) {
					contactDetailsFragment = ContactDetailsFragment
							.newInstance(currentContactUri);
					super.getFragmentManager()
							.beginTransaction()
							.replace(R.id.contact_details,
									contactDetailsFragment)
							.setTransition(
									FragmentTransaction.TRANSIT_FRAGMENT_FADE)
							.commit();
				}
			} else {
				Intent intent = new Intent();
				intent.setClass(super.getActivity().getApplicationContext(),
						ContactDetailsActivity.class);
				intent.putExtra("contactUri", currentContactUri);
				super.startActivity(intent);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader(" + id + ", " + args + ")");
		return new CursorLoader(super.getActivity().getApplicationContext(),
				Contacts.CONTENT_URI, CONTACTS_SUMMARY_PROJECTION,
				CONTACTS_SUMMARY_SELECTION, null, CONTACTS_SUMMARY_SORT_ORDER);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "onLoadFinished(" + loader.getId() + "," + data.getCount()
				+ " rows)");
		this.simpleCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset(...)");
		this.simpleCursorAdapter.swapCursor(null);
	}
}
