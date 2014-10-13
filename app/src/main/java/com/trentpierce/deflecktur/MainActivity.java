package com.trentpierce.deflecktur;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.trentpierce.deflecktur.db.Phone;
import com.trentpierce.deflecktur.db.PhoneProvider;
import com.trentpierce.deflecktur.db.PhoneTable;

public class MainActivity extends Activity {

	protected static final String TAG = MainActivity.class.getSimpleName();

	private TextView mTextView;
	private TextView mTextViewExp;
	private ListView mListView;

	private SparseArray<Phone> mList = null;
	private PhoneAdapter mListAdapter;
	private ContentObserver mObserver;

	private boolean mPaused;

	private SparseArray<Phone> mPhonesToUpdate = new SparseArray<Phone>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		ActionBar actionBar = getActionBar();
		actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#FF6A00")));
	    actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//		ArrayList<String> phones = CallReceiver.getInstance().getPhones(this);
		fillData(false);

		mListView = (ListView) findViewById(R.id.lv_phones);
		mListAdapter = new PhoneAdapter(this, mList);
		mListView.setAdapter(mListAdapter);
		
		mTextViewExp = (TextView) findViewById(R.id.explanation);
		if (mList.size() == 0) {
		mTextViewExp.setText("You have not saved any numbers to block");
		}
		mTextView = (TextView) findViewById(R.id.et_phone_number);
		if (mTextView != null) {
			mTextView.setText(mList.get(0).getPhoneNumber());
			mTextView.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

				@Override
				public void afterTextChanged(Editable s) {
					Phone phone = new Phone(0, s.toString());
					update(phone);
				}
			});
		}
	}

	@Override
	protected void onResume() {
		mPaused = false;
		super.onResume();
		startObserving();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopObserving();
		mPaused = true;
		updateAll(false);
	}

	private void startObserving() {
		if (mObserver == null) {
			mObserver = new ContentObserver(new Handler()) {

				@Override
				public void onChange(boolean selfChange) {
					onChange(selfChange, null);
				}

				@Override
				public void onChange(boolean selfChange, Uri uri) {
					// Reload list
					fillData(true);
				}
			};
		}
		getContentResolver().registerContentObserver(PhoneProvider.CONTENT_URI, true, mObserver);
	}

	private void stopObserving() {
		getContentResolver().unregisterContentObserver(mObserver);
	}

	public boolean isPaused() {
		return mPaused;
	}

	protected void fillData(boolean reload) {
		mList = CallReceiver.getInstance().getPhones(this, reload);
		if (mListAdapter != null)
			mListAdapter.notifyDataSetChanged();
	}

	public void update(Phone phone) {
		mPhonesToUpdate.put(phone.getId(), phone);
		CallReceiver.getInstance().setPhone(this, phone.getId(), phone);
		if (isPaused()) {
			updateAll(false);
		}
	}

	public void updateAll(boolean force) {
		if (force)
			stopObserving();
		for (int i = 0; i < mPhonesToUpdate.size(); i++) {
			Phone phone = mPhonesToUpdate.valueAt(i);
			Uri uri = Uri.parse(PhoneProvider.CONTENT_URI + "/"
					+ phone.getId());
			ContentValues values = new ContentValues();
			values.put(PhoneTable.COLUMN_ID, phone.getId());
			values.put(PhoneTable.COLUMN_PHONE_NUMBER, phone.getPhoneNumber());
			getContentResolver().update(uri, values, null, null);
		}
		mPhonesToUpdate.clear();
		if (force && !isPaused())
			startObserving();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case (android.R.id.home):
			Intent intentHome=new Intent(getApplicationContext(),StartActivity.class);
        startActivity(intentHome);
		default:
            return super.onOptionsItemSelected(item);
	}
  }
}
