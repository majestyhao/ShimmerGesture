package edu.ucdavis.myshimmerapp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshimmerapp.R;

import java.io.File;
import java.io.FilenameFilter;

public class UserActivity extends Activity {
	private static final String TAG = "MyShimmerApp.BTDeviceListActivity";
	private final static String AppFilePath = Environment
			.getExternalStorageDirectory() + "/ShimmerTest/Users";
	// Return Intent extra
	public static String EXTRA_USER_NAME = "user_name";

	private ArrayAdapter<String> mExistingUserArrayAdapter;
	private Button newUserButton;
	private Dialog mDialog;
	private EditText mEditText;

	private String mUserName;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup the window
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.btdevicelist);

		mDialog = new Dialog(this);
		// Initialize the button to perform device discovery
		newUserButton = (Button) findViewById(R.id.button_scan);

		newUserButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDialog.setContentView(R.layout.new_user_dialog);
				mEditText = (EditText) findViewById(R.id.new_use_edit_text);
				Button doneButton = (Button) findViewById(R.id.button_done);
				doneButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String str = mEditText.getText().toString();
						if (str != null && str.length() != 0) {
							mUserName = str;
						}
					}
				});
				Button cancelButton = (Button) findViewById(R.id.button_cancel);
			}
		});

		mExistingUserArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.btdeviceitem);

		// Find and set up the ListView for paired devices
		ListView existingUserListView = (ListView) findViewById(R.id.existing_users);
		existingUserListView.setAdapter(mExistingUserArrayAdapter);
		existingUserListView.setOnItemClickListener(mUserNameClickListener);

		ListDir();
	}

	// The on-click listener for all devices in the ListViews
	private OnItemClickListener mUserNameClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
			mUserName = ((TextView) v).getText().toString();

			// Create the result Intent and include the MAC address
			Intent intent = new Intent();
			intent.putExtra(EXTRA_USER_NAME, mUserName);

			Toast.makeText(getApplicationContext(),
					"User Selected " + "-> " + mUserName, Toast.LENGTH_SHORT)
					.show();
			setResult(Activity.RESULT_OK, intent); // Set result and finish this
													// Activity

			finish();
		}

	};

	private void ListDir() {
		File file = new File(AppFilePath);
		if (!file.exists()) {
			file.mkdir();
			return;
		}

		String[] directories = file.list(new FilenameFilter() {
			@Override
			public boolean accept(File current, String name) {
				return new File(current, name).isDirectory();
			}
		});

		if (directories != null && directories.length != 0) {
			for (String s : directories)
				mExistingUserArrayAdapter.add(s);
		}
	}
}
