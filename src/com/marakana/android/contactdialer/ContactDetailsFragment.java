package com.marakana.android.contactdialer;

import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class ContactDetailsFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	private static final String TAG = "ContactDetailsFragment";

	private static final int CONTACTS_DETAILS_LOADER = 0;

	private static final int PHONE_NUMBER_LOADER = 1;

	private static final String[] CONTACTS_DETAILS_PROJECTION = new String[] {
			Contacts._ID, Contacts.DISPLAY_NAME };

	private static final String[] PHONE_NUMBER_PROJECTION = { Contacts._ID,
			CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE };

	private static final String PHONE_NUMBER_SELECTION = Data.MIMETYPE + "='"
			+ CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND "
			+ Data.CONTACT_ID + "= ?";

	private static final String[] FROM_COLUMNS_NAMES = {
			CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.TYPE };

	private static final int[] TO_VIEW_IDS = { R.id.phone, R.id.type };

	public static ContactDetailsFragment newInstance(Uri contactUri) {
		Log.d(TAG, "newInstance(" + contactUri + ")");
		ContactDetailsFragment contactDetailsFragment = new ContactDetailsFragment();
		Bundle args = new Bundle();
		args.putParcelable("contactUri", contactUri);
		contactDetailsFragment.setArguments(args);
		return contactDetailsFragment;
	}

	private SimpleCursorAdapter simpleCursorAdapter;

	private TextView contactNameView;

	public Uri getContactUri() {
		return getArguments().getParcelable("contactUri");
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView(...)");
		if (container == null) {
			return null;
		}
		View view = inflater.inflate(R.layout.contact_details_fragment,
				container, false);
		this.contactNameView = (TextView) view.findViewById(R.id.contact_name);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "onActivityCreated(" + savedInstanceState + ")");
		super.onActivityCreated(savedInstanceState);
		this.simpleCursorAdapter = new SimpleCursorAdapter(getActivity()
				.getApplicationContext(), R.layout.contact_phone_list_row,
				null, FROM_COLUMNS_NAMES, TO_VIEW_IDS, 0);
		this.simpleCursorAdapter.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Cursor cursor,
					int columnIndex) {
				if (view.getId() == R.id.type) {
					int type = cursor.getInt(columnIndex);
					CharSequence typeLabel = Phone.getTypeLabel(
							ContactDetailsFragment.this.getResources(), type,
							"Unknown");
					((TextView) view).setText(typeLabel);
					return true;
				} else {
					return false;
				}
			}
		});
		super.setListAdapter(this.simpleCursorAdapter);
		super.getLoaderManager()
				.initLoader(CONTACTS_DETAILS_LOADER, null, this);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(TAG, "onListItemClick(" + l + ", " + v + ", " + position + ", "
				+ id + ")");
		TextView view = (TextView) v.findViewById(R.id.phone);
		String phone = view.getText().toString();
		Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:" + phone));
		super.startActivity(intent);
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		super.onDestroy();
		this.getLoaderManager().destroyLoader(CONTACTS_DETAILS_LOADER);
		this.getLoaderManager().destroyLoader(PHONE_NUMBER_LOADER);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader(" + id + ", " + args + ")");
		Context context = super.getActivity().getApplicationContext();
		switch (id) {
		case CONTACTS_DETAILS_LOADER:
			return new CursorLoader(context, this.getContactUri(),
					CONTACTS_DETAILS_PROJECTION, null, null, null);
		case PHONE_NUMBER_LOADER:
			int contactId = args.getInt("contactId");
			String[] selectionArgs = { String.valueOf(contactId) };
			return new CursorLoader(context, Data.CONTENT_URI,
					PHONE_NUMBER_PROJECTION, PHONE_NUMBER_SELECTION,
					selectionArgs, null);
		default:
			throw new AssertionError("Unexpected id: " + id);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		Log.d(TAG, "onLoadFinished(" + loader.getId() + "," + data.getCount()
				+ " rows)");
		switch (loader.getId()) {
		case CONTACTS_DETAILS_LOADER:
			if (data.moveToFirst()) {
				int contactId = data.getInt(data.getColumnIndex(Contacts._ID));
				String contactName = data.getString(data
						.getColumnIndex(Contacts.DISPLAY_NAME));
				Log.d(TAG, "Loaded contact with id=" + contactId + " and name="
						+ contactName);
				this.contactNameView.setText(contactName);
				Bundle bundle = new Bundle();
				bundle.putInt("contactId", contactId);
				super.getLoaderManager().initLoader(PHONE_NUMBER_LOADER,
						bundle, this);
			} else {
				Log.d(TAG, "No contact details");
			}
			break;
		case PHONE_NUMBER_LOADER:
			this.simpleCursorAdapter.swapCursor(data);
			break;
		default:
			throw new AssertionError("Unexpected loader: " + loader.getId());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		Log.d(TAG, "onLoaderReset(" + loader.getId() + ", ...)");
		switch (loader.getId()) {
		case CONTACTS_DETAILS_LOADER:
			this.contactNameView.setText("");
			break;
		case PHONE_NUMBER_LOADER:
			this.simpleCursorAdapter.swapCursor(null);
			break;
		default:
			throw new AssertionError("Unexpected loader: " + loader.getId());
		}
	}
}
