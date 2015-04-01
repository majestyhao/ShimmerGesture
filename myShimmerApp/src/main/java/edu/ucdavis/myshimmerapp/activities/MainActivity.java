package edu.ucdavis.myshimmerapp.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.xy.XYPlot;
import com.example.myshimmerapp.R;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.myshimmerapp.services.MyShimmerService;
import pl.flex_it.androidplot.XYSeriesShimmer;

public class MainActivity extends MyServiceActivity {
	private static final String TAG = "MyShimmerApp.MainActivity";

	final static int X_AXIS_LENGTH = 500;
	public static HashMap<String, List<Number>> mPlotDataMap = new HashMap<String, List<Number>>(
			3);
	public static HashMap<String, XYSeriesShimmer> mPlotSeriesMap = new HashMap<String, XYSeriesShimmer>(
			3);

	static final int REQUEST_ENABLE_BT = 1;
	static final int REQUEST_CONNECT_SHIMMER = 2;
	static final int REQUEST_SETTINGS = 3;

	public static final int GESTURE_TYPE_FINGER = 0;
	public static final int GESTURE_TYPE_HAND = 1;
	public static final int GESTURE_TYPE_ARM = 2;

	public static final int RECOG_MODE_WINDOWED = 0;
	public static final int RECOG_MODE_CONTINOUS = 1;

	public static final int ML_ALGORITHM_SIMPLE_LOGISTIC = 0;
	public static final int ML_ALGORITHM_DECISION_TREE = 1;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Name of the connected device
	private static String mBluetoothAddress = null;

	private static TextView deviceAddrContent = null;
	private static TextView deviceStatContent = null;

	private static Button connectionButton = null;
	private static Button settingButton = null;
	private static Button streamingButton = null;
	private static Button recogButton = null;
	private static Button trainingButton = null;

	private static int sensorType = SENSOR_TYPE_ACCL;
	private static int gestureType = GESTURE_TYPE_FINGER;
	private static int recogMode = RECOG_MODE_CONTINOUS;
	private static int mlAlgo = ML_ALGORITHM_SIMPLE_LOGISTIC;
	// private static int windows[] = new int[2];
	private static double samplingRate = 0;

	private static RadioButton acclButton = null;
	private static RadioButton gyroButton = null;

	private static XYPlot dynamicPlot;

	// protected static Timer mPrepTimer;
	// protected static final String mPrepTimerName = "MainActivity.PrepTimer";
	// protected static boolean isPrepTimerRunning = false;
	// protected static int mPrepWindowSize = 15000;
	// protected static double[] prepDataAvg = new double[12];
	// protected static MyShimmerDataList mPrepData = new MyShimmerDataList();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "On Create");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this,
					"Device does not support Bluetooth\nExiting...",
					Toast.LENGTH_LONG).show();
			finish();
		}
		doBindService();

		deviceAddrContent = (TextView) findViewById(R.id.textDeviceContent);
		deviceStatContent = (TextView) findViewById(R.id.textStateContent);

		connectionButton = (Button) findViewById(R.id.button_connection);
		connectionButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "ConnectionButton:" + mService.isDeviceConnected());
				if (!mService.isDeviceConnected()) {
					Intent serverIntent = new Intent(getApplicationContext(),
							BTDeviceListActivity.class);
					startActivityForResult(serverIntent,
							REQUEST_CONNECT_SHIMMER);
				} else {
					mService.discconnectShimmer();
				}
				connectionButton.setEnabled(false);
			}
		});

		streamingButton = (Button) findViewById(R.id.button_streaming);
		streamingButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "StreammingButton:" + mService.isStreamming());
				if (mService.isStreamming()) {
					mService.stopStreaming();
					// if (isPrepTimerRunning) {
					// cancelTimer(mPrepTimer);
					// mPrepTimer = null;
					// isPrepTimerRunning = false;
					// mPrepData.clear();
					// }

				} else {
					mService.startStreaming();
					// mPrepTimer = startTimer(mPrepTimerName, 5000, false,
					// mActivityHandler);
					// if (mPrepTimer != null)
					// isPrepTimerRunning = true;
				}
			}
		});
		streamingButton.setEnabled(false);

		recogButton = (Button) findViewById(R.id.button_recognition);
		recogButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "recogButton:" + mService.isStreamming());
				if (mService.isStreamming()) {
					// if (isPrepTimerRunning) {
					// cancelTimer(mPrepTimer);
					// mPrepTimer = null;
					// isPrepTimerRunning = false;
					// calcPrepDataAvg();
					// }
					Intent serverIntent;
					if (recogMode == RECOG_MODE_CONTINOUS) {
						serverIntent = new Intent(getApplicationContext(),
								RecogContinousActivity.class);
						// serverIntent.putExtra(
						// RecogIntermittentActivity.Extra_Window_Sizes,
						// windows);
					} else {
						serverIntent = new Intent(getApplicationContext(),
								RecogIntermittentActivity.class);
					}
					// serverIntent
					// .putExtra(RecogTrainActivityBase.Extra_Prep_data,
					// prepDataAvg);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_Sampling_Rate,
							samplingRate);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_Gesture_Type,
							gestureType);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_ML_Algo, mlAlgo);
					startActivity(serverIntent);
				}
			}
		});
		recogButton.setEnabled(false);

		trainingButton = (Button) findViewById(R.id.button_training);
		trainingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "trainingButton:" + mService.isStreamming());
				if (mService.isStreamming()) {

					Intent serverIntent = new Intent(getApplicationContext(),
							TrainingActivity.class);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_Sampling_Rate,
							samplingRate);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_Gesture_Type,
							gestureType);
					serverIntent.putExtra(
							SettingsActivity.Settings_Extra_ML_Algo, mlAlgo);
					startActivity(serverIntent);
				}
			}

		});
		trainingButton.setEnabled(false);

		settingButton = (Button) findViewById(R.id.button_settings);
		settingButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "settingButton");
				Intent serverIntent = new Intent(getApplicationContext(),
						SettingsActivity.class);
				serverIntent.putExtra(
						SettingsActivity.Settings_Extra_Gesture_Type,
						gestureType);
				serverIntent.putExtra(
						SettingsActivity.Settings_Extra_Recog_Mode, recogMode);
				serverIntent.putExtra(SettingsActivity.Settings_Extra_ML_Algo,
						mlAlgo);
				// serverIntent.putExtra(
				// SettingsActivity.Settings_Extra_Window_Sizes, windows);
				startActivityForResult(serverIntent, REQUEST_SETTINGS);
			}
		});
		settingButton.setEnabled(false);

		acclButton = (RadioButton) findViewById(R.id.button_accl);
		acclButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "acclButton");
				acclButton.setChecked(true);
				gyroButton.setChecked(false);
				sensorType = SENSOR_TYPE_ACCL;
			}
		});

		gyroButton = (RadioButton) findViewById(R.id.button_gyro);
		gyroButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(TAG, "gyroButton");
				acclButton.setChecked(false);
				gyroButton.setChecked(true);
				sensorType = SENSOR_TYPE_GYRO;
			}
		});

		if (sensorType == SENSOR_TYPE_ACCL) {
			acclButton.setChecked(true);
			gyroButton.setChecked(false);
		} else if (sensorType == SENSOR_TYPE_GYRO) {
			acclButton.setChecked(false);
			gyroButton.setChecked(true);
		}

		dynamicPlot = (XYPlot) findViewById(R.id.dynamicPlot);
		dynamicPlot.setTitle("Shimmer Data Plot");
		dynamicPlot.setBackgroundColor(Color.TRANSPARENT);
		dynamicPlot.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("0"));

		dynamicPlot.getBackgroundPaint().setColor(Color.WHITE);
		dynamicPlot.setBackgroundColor(Color.WHITE);
		dynamicPlot.getGraphWidget().getGridBackgroundPaint()
				.setColor(Color.WHITE);
		dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		dynamicPlot.getGraphWidget().getDomainOriginLinePaint()
				.setStrokeWidth(3);
		dynamicPlot.getGraphWidget().getRangeOriginLinePaint()
				.setStrokeWidth(3);
		
//		dynamicPlot.getLegendWidget().getTextPaint().setTextSize(12);
//		dynamicPlot.getTitleWidget().getLabelPaint().setTextSize(16);
	}

	public static Handler mActivityHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MyShimmerService.Message_ShimmerStatusChange:
				Log.d(TAG, "shimmer status change" + msg.arg1);
				switch (msg.arg1) {
				case Shimmer.STATE_CONNECTED:
					if (mBluetoothAddress != null)
						deviceAddrContent.setText(mBluetoothAddress);
					deviceStatContent.setText(R.string.stat_connected);
					connectionButton.setEnabled(true);
					connectionButton.setText(R.string.button_disconnect);
					samplingRate = mService.getShimmerSampleRate();
					Log.d(TAG, "samplingRate:" + samplingRate);
					break;

				case Shimmer.MSG_STATE_FULLY_INITIALIZED:
					streamingButton.setEnabled(true);
					streamingButton.setText(R.string.button_startstreaming);
					settingButton.setEnabled(true);
					break;

				case Shimmer.STATE_CONNECTING:
					deviceStatContent.setText(R.string.stat_connecting);
					break;

				case Shimmer.MSG_STATE_STREAMING:
					streamingButton.setText(R.string.button_stopstreaming);
					settingButton.setEnabled(false);
					recogButton.setEnabled(true);
					trainingButton.setEnabled(true);
					break;

				case Shimmer.MSG_STATE_STOP_STREAMING:
					streamingButton.setText(R.string.button_startstreaming);
					recogButton.setEnabled(false);
					trainingButton.setEnabled(false);
					settingButton.setEnabled(true);
					break;

				case Shimmer.STATE_NONE:
					deviceAddrContent.setText("00:00:00:00:00:00");
					deviceStatContent.setText(R.string.stat_disconnected);
					connectionButton.setEnabled(true);
					connectionButton.setText(R.string.button_connect);
					streamingButton.setEnabled(false);
					streamingButton.setText(R.string.button_startstreaming);
					recogButton.setEnabled(false);
					trainingButton.setEnabled(false);
					settingButton.setEnabled(false);
					break;
				}
				break;

			case MyShimmerService.Message_ShimmerRead:
				if ((msg.obj instanceof ObjectCluster)) {
					double[] data = MyShimmerDataList
							.parseShimmerObject((ObjectCluster) msg.obj);
					drawGraph(dynamicPlot, mPlotDataMap, mPlotSeriesMap,
							sensorType, data);

					// addPrepData(data);
				}
				break;

			case MyShimmerService.Message_ServiceConnected:
				Log.d(TAG, "Message_ServiceConnected");
				updateView();
				break;

			case MyShimmerService.Message_TimerCallBack:
				Log.d(TAG, "Message_TimerCallBack");
				if (msg.obj instanceof String) {
					// String timerName = (String) msg.obj;
					// if (timerName.equals(mPrepTimerName)) {
					// calcPrepDataAvg();
					// isPrepTimerRunning = false;
					// recogButton.setEnabled(true);
					// trainingButton.setEnabled(true);
					// }
				}
				break;
			}
		}
	};

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");
		if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
			Log.d(TAG, "start bluetooth");
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause");
		mPlotSeriesMap.clear();
		mPlotDataMap.clear();
		dynamicPlot.clear();

		// cancelTimer(mPrepTimer);
		// mPrepTimer = null;
		// isPrepTimerRunning = false;
		// mPrepData.clear();

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
	}

	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
		Log.d(TAG, "onDestroy1");
		// finish();
		mPlotSeriesMap.clear();
		mPlotDataMap.clear();
		dynamicPlot.clear();

		// cancelTimer(mPrepTimer);
		// mPrepTimer = null;
		// isPrepTimerRunning = false;
		// mPrepData.clear();

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");

		doBindService();
		mPlotSeriesMap.clear();
		mPlotDataMap.clear();
		dynamicPlot.clear();

		// cancelTimer(mPrepTimer);
		// mPrepTimer = null;
		// isPrepTimerRunning = false;
		// mPrepData.clear();

		if (mService != null)
			mService.registerGraphHandler(mActivityHandler);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_ENABLE_BT:
			if (resultCode == Activity.RESULT_OK) {
				Toast.makeText(this, "Bluetooth is now enabled",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(this, "Bluetooth not enabled\nExiting...",
						Toast.LENGTH_SHORT).show();
				finish();
			}
			break;

		case REQUEST_CONNECT_SHIMMER:
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(
						BTDeviceListActivity.EXTRA_DEVICE_ADDRESS);
				Log.d(TAG, "connection request to: " + address);
				Log.d(TAG, "mService:" + mService);
				mBluetoothAddress = address;
				mService.connectShimmer(mBluetoothAddress);
				mService.registerGraphHandler(mActivityHandler);
			}
			break;

		case REQUEST_SETTINGS:
			if (resultCode == Activity.RESULT_OK) {

				String rate = data.getExtras().getString(
						SettingsActivity.Settings_Extra_Sampling_Rate);
				String gesture = data.getExtras().getString(
						SettingsActivity.Settings_Extra_Gesture_Type);
				String recog = data.getExtras().getString(
						SettingsActivity.Settings_Extra_Recog_Mode);
				String mlalgo = data.getExtras().getString(
						SettingsActivity.Settings_Extra_ML_Algo);
				// String[] windowsStr = data.getExtras().getStringArray(
				// SettingsActivity.Settings_Extra_Window_Sizes);

				Log.d(TAG, "gestureType:" + gesture + ";" + "samplingRate:"
						+ rate + "recogMode:" + recog);
				if (samplingRate != Double.parseDouble(rate)) {
					samplingRate = Double.parseDouble(rate);
					mService.setShimmerSampleRate(samplingRate);
				}

				int i = 0;
				for (i = 0; i < SettingsActivity.gesture_types.length; i++) {
					if (gesture.equals(SettingsActivity.gesture_types[i]))
						break;
				}
				gestureType = i;
				for (i = 0; i < SettingsActivity.recog_modes.length; i++) {
					if (recog.equals(SettingsActivity.recog_modes[i]))
						break;
				}
				recogMode = i;
				for (i = 0; i < SettingsActivity.ml_algos.length; i++) {
					if (mlalgo.equals(SettingsActivity.ml_algos[i]))
						break;
				}
				mlAlgo = i;

				// if (recogMode == RECOG_MODE_WINDOWED) {
				// if (windowsStr != null) {
				// if (windowsStr[0] != null) {
				// windows[0] = Integer.parseInt(windowsStr[0]);
				// }
				// if (windowsStr[1] != null) {
				// windows[1] = Integer.parseInt(windowsStr[1]);
				// }
				// }
				// }
			}
			break;
		}
	}

	private static void updateView() {
		Log.d(TAG, "updateView");
		if (deviceAddrContent != null && mBluetoothAddress != null) {
			deviceAddrContent.setText(mBluetoothAddress);
		}
		if (deviceStatContent != null && connectionButton != null
				&& streamingButton != null && recogButton != null
				&& trainingButton != null && mService != null) {
			if (mService.isDeviceConnected()) {
				deviceStatContent.setText(R.string.stat_connected);
				connectionButton.setText(R.string.button_disconnect);
				streamingButton.setEnabled(true);
				if (mService.isStreamming()) {
					streamingButton.setText(R.string.button_stopstreaming);
					recogButton.setEnabled(true);
					trainingButton.setEnabled(true);
					settingButton.setEnabled(false);
				} else {
					streamingButton.setText(R.string.button_startstreaming);
					recogButton.setEnabled(false);
					trainingButton.setEnabled(false);
					settingButton.setEnabled(true);
				}
			} else {
				deviceStatContent.setText(R.string.stat_disconnected);
				connectionButton.setText(R.string.button_connect);
				streamingButton.setEnabled(false);
				settingButton.setEnabled(false);
			}
		}
	}

	// protected static void addPrepData(double[] datas) {
	// if (isPrepTimerRunning) {
	// // Log.d(TAG, "addPrepData");
	// mPrepData.add(datas);
	// }
	// }

	// private static void calcPrepDataAvg() {
	//
	// if (mPrepData != null && !mPrepData.isEmpty()) {
	// int dataSize = mPrepData.size();
	// Log.d(TAG, "dataSize:" + dataSize);
	// double sumXAccl = 0, sumYAccl = 0, sumZAccl = 0;
	// double sumXGyro = 0, sumYGyro = 0, sumZGyro = 0;
	// double meanXAccl = 0, meanYAccl = 0, meanZAccl = 0;
	// double meanXGyro = 0, meanYGyro = 0, meanZGyro = 0;
	// double varXAccl = 0, varYAccl = 0, varZAccl = 0;
	// double varXGyro = 0, varYGyro = 0, varZGyro = 0;
	// double sdXAccl = 0, sdYAccl = 0, sdZAccl = 0;
	// double sdXGyro = 0, sdYGyro = 0, sdZGyro = 0;
	// //
	// // for (MyShimmerDataList d : mPrepData) {
	// // sumXAccl += d.get(0);
	// // sumYAccl += d.get(1);
	// // sumZAccl += d.get(2);
	// // sumXGyro += d.get(3);
	// // sumYGyro += d.get(4);
	// // sumZGyro += d.get(5);
	// // }
	// // meanXAccl = sumXAccl / dataSize;
	// // meanYAccl = sumYAccl / dataSize;
	// // meanZAccl = sumZAccl / dataSize;
	// // meanXGyro = sumXGyro / dataSize;
	// // meanYGyro = sumYGyro / dataSize;
	// // meanZGyro = sumZGyro / dataSize;
	// //
	// // for (MyShimmerDataList d : mPrepData) {
	// // varXAccl += (meanXAccl - d.get(0)) * (meanXAccl - d.get(0));
	// // varYAccl += (meanYAccl - d.get(1)) * (meanYAccl - d.get(1));
	// // varZAccl += (meanZAccl - d.get(2)) * (meanZAccl - d.get(2));
	// // varXGyro += (meanXGyro - d.get(3)) * (meanXGyro - d.get(3));
	// // varYGyro += (meanYGyro - d.get(4)) * (meanYGyro - d.get(4));
	// // varZGyro += (meanZGyro - d.get(5)) * (meanZGyro - d.get(5));
	// // }
	// // varXAccl = varXAccl / dataSize;
	// // varYAccl = varYAccl / dataSize;
	// // varZAccl = varZAccl / dataSize;
	// // varXGyro = varXGyro / dataSize;
	// // varYGyro = varYGyro / dataSize;
	// // varZGyro = varZGyro / dataSize;
	// //
	// // sdXAccl = Math.sqrt(varXAccl);
	// // sdYAccl = Math.sqrt(varYAccl);
	// // sdZAccl = Math.sqrt(varZAccl);
	// // sdXGyro = Math.sqrt(varXGyro);
	// // sdYGyro = Math.sqrt(varYGyro);
	// // sdZGyro = Math.sqrt(varZGyro);
	// //
	// // Log.d(TAG, "mean accl: " + meanXAccl + ";" + meanYAccl + ";"
	// // + meanZAccl);
	// // Log.d(TAG, "mean gyro: " + meanXGyro + ";" + meanYGyro + ";"
	// // + meanZGyro);
	// // Log.d(TAG, "sd accl: " + sdXAccl + ";" + sdYAccl + ";" +
	// // sdZAccl);
	// // Log.d(TAG, "sd gyro: " + sdXGyro + ";" + sdYGyro + ";" +
	// // sdZGyro);
	//
	// // prepDataAvg[0] = meanXAccl;
	// // prepDataAvg[1] = meanYAccl;
	// // prepDataAvg[2] = meanZAccl;
	// // prepDataAvg[3] = meanXGyro;
	// // prepDataAvg[4] = meanYGyro;
	// // prepDataAvg[5] = meanZGyro;
	// // prepDataAvg[6] = sdXAccl;
	// // prepDataAvg[7] = sdYAccl;
	// // prepDataAvg[8] = sdZAccl;
	// // prepDataAvg[9] = sdXGyro;
	// // prepDataAvg[10] = sdYGyro;
	// // prepDataAvg[11] = sdZGyro;
	// }
	// mPrepData.clear();
	// }

}
