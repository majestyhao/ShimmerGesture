package edu.ucdavis.myshimmerapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.example.myshimmerapp.R;

public class SettingsActivity extends MyServiceActivity {

	private static final String TAG = "MyShimmerApp.SettingsActivity";

	public static final String Settings_Extra_Gesture_Type = "Settings_GestureType";
	public static final String Settings_Extra_Sampling_Rate = "Settings_SamplingRate";
	public static final String Settings_Extra_ML_Algo = "Settings_MLAlgorithm";
	public static final String Settings_Extra_Recog_Mode = "Settings_RecogMode";
	// public static final String Settings_Extra_Window_Sizes = "WindowSizes";

	public static final String[] sampling_rates = { "8", "16", "51.2", "102.4",
			"128", "204.8", "256", "512", "1024" };
	public static final String[] gesture_types = { "Finger", "Hand", "Writing" };
	public static final String[] ml_algos = { "Simple Logistic",
			"Decision Tree" };
	public static final String[] recog_modes = { "Windowed", "Continous" };

	private Button doneButton;
	private Button cancelButton;
	private RadioGroup samplingGroup;
	private RadioGroup gestureGroup;
	private RadioGroup mlalgoGroup;
	private RadioGroup recogGroup;

	// private LinearLayout textLayout;
	// private EditText big_Win_Size_Text;
	// private EditText small_Win_Size_Text;

	String gesture;
	String rate;
	String recog;
	String mlalgo;

	// int[] windows = new int[2];

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.settings_2);

		setResult(Activity.RESULT_CANCELED);

		doneButton = (Button) findViewById(R.id.button_done);
		doneButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				Log.d(TAG, "rate:" + rate);
				Log.d(TAG, "gesture:" + gesture);

				intent.putExtra(Settings_Extra_Sampling_Rate, rate);
				intent.putExtra(Settings_Extra_Gesture_Type, gesture);
				intent.putExtra(Settings_Extra_Recog_Mode, recog);
				intent.putExtra(Settings_Extra_ML_Algo, mlalgo);
				// if (recog.equals(recog_modes[0])) {
				// if (big_Win_Size_Text.getText() != null)
				// windows[0] = Integer.parseInt(big_Win_Size_Text
				// .getText().toString());
				// if (small_Win_Size_Text.getText() != null)
				// windows[1] = Integer.parseInt(small_Win_Size_Text
				// .getText().toString());
				// intent.putExtra(Settings_Extra_Window_Sizes, windows);
				// }
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		cancelButton = (Button) findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});

		samplingGroup = (RadioGroup) findViewById(R.id.sample_rate_group);
		gestureGroup = (RadioGroup) findViewById(R.id.gesture_type_group);
		mlalgoGroup = (RadioGroup) findViewById(R.id.ml_algo_group);
		recogGroup = (RadioGroup) findViewById(R.id.recog_mode_group);

		// set initial value
		String currentRate = String.valueOf(mService.getShimmerSampleRate());
		Log.d(TAG, "currentRate:" + currentRate);
		for (String str : sampling_rates) {
			RadioButton radio = new RadioButton(this);
			radio.setText(str);
			samplingGroup.addView(radio);
			if (currentRate.contains(str)) {
				radio.setChecked(true);
				rate = radio.getText().toString();
			}
		}

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			gesture = gesture_types[extras.getInt(Settings_Extra_Gesture_Type)];
			recog = recog_modes[extras.getInt(Settings_Extra_Recog_Mode)];
			mlalgo = ml_algos[extras.getInt(Settings_Extra_ML_Algo)];
			// windows = extras.getIntArray(Settings_Extra_Window_Sizes);
		}
		for (String str : gesture_types) {
			RadioButton radio = new RadioButton(this);
			radio.setText(str);

			gestureGroup.addView(radio);
			if (gesture != null && gesture.equals(str)) {
				radio.setChecked(true);
			}
		}
		for (String str : recog_modes) {
			RadioButton radio = new RadioButton(this);
			radio.setText(str);

			recogGroup.addView(radio);
			if (recog != null && recog.equals(str)) {
				radio.setChecked(true);
			}
		}
		for (String str : ml_algos) {
			RadioButton radio = new RadioButton(this);
			radio.setText(str);

			mlalgoGroup.addView(radio);
			if (mlalgo != null && mlalgo.equals(str)) {
				radio.setChecked(true);
			}
		}

		samplingGroup.setOnCheckedChangeListener(mCheckedChangeListener1);
		gestureGroup.setOnCheckedChangeListener(mCheckedChangeListener1);
		recogGroup.setOnCheckedChangeListener(mCheckedChangeListener1);
		mlalgoGroup.setOnCheckedChangeListener(mCheckedChangeListener1);
		// textLayout = (LinearLayout) findViewById(R.id.Recog_mode_text);
		// textLayout.setVisibility(View.GONE);

		// big_Win_Size_Text = (EditText) findViewById(R.id.big_window_size);
		// small_Win_Size_Text = (EditText)
		// findViewById(R.id.small_window_size);
		//
		// if (windows != null) {
		//
		// big_Win_Size_Text.setText("0");
		// small_Win_Size_Text.setText("0");
		// }
		// big_Win_Size_Text.setVisibility(View.INVISIBLE);
		// small_Win_Size_Text.setVisibility(View.INVISIBLE);

		// big_Win_Size_Text.setOnFocusChangeListener(mFocusChangeListener1);
		// small_Win_Size_Text.setOnFocusChangeListener(mFocusChangeListener2);
	}

	private OnCheckedChangeListener mCheckedChangeListener1 = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			Log.d(TAG, "group:" + group + ";id:" + checkedId);
			RadioButton rb = (RadioButton) findViewById(checkedId);
			if (rb != null) {
				if (group == samplingGroup) {
					rate = rb.getText().toString();
					Log.d(TAG, rate);
				} else if (group == gestureGroup) {
					gesture = rb.getText().toString();
					Log.d(TAG, gesture);
				} else if (group == recogGroup) {
					recog = rb.getText().toString();
					Log.d(TAG, recog);
				} else if (group == mlalgoGroup) {
					mlalgo = rb.getText().toString();
					Log.d(TAG, mlalgo);
				}
			}
		}
	};

	// private OnFocusChangeListener mFocusChangeListener1 = new
	// OnFocusChangeListener() {
	//
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (v instanceof EditText) {
	// EditText t = (EditText) v;
	// if (hasFocus) {
	// t.setText("");
	// } else if (t.getText() != null) {
	// t.setText("# of small windows/big window");
	// }
	// }
	// }
	// };
	//
	// private OnFocusChangeListener mFocusChangeListener2 = new
	// OnFocusChangeListener() {
	//
	// public void onFocusChange(View v, boolean hasFocus) {
	// if (v instanceof EditText) {
	// EditText t = (EditText) v;
	// if (hasFocus) {
	// t.setText("");
	// } else if (t.getText() != null) {
	// t.setText("# of shimmer datas/small window");
	// }
	// }
	// }
	// };
}
