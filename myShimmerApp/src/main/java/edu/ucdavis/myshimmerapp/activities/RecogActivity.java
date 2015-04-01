package edu.ucdavis.myshimmerapp.activities;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.androidplot.xy.XYPlot;
import com.example.myshimmerapp.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.myshimmerapp.ml.Features;
import edu.ucdavis.myshimmerapp.ml.Features.FeatureClass;
import pl.flex_it.androidplot.XYSeriesShimmer;

public abstract class RecogActivity extends MyServiceActivity {

	private static final String TAG = "MyShimmerApp.RecogActivity";

	public final static String Extra_Prep_data = "Extra_PrepData";
	public final static String Extra_Sampling_Rate = "Extra_Sampling_Rate";

	private static double samplingRate = 0;
	protected static int mWindowCounter = 0;
	protected final static int mWindowSize = 16;

	protected static int mEndingWindowCounter = 0;
	protected final static int mEndingWindowMax = 4;
	protected static int mEndingPoint = 0;

	protected static MyShimmerDataList mRecordData = new MyShimmerDataList();
	protected static boolean mIsRecording = false;

	protected static HashMap<String, List<Number>> mPlotAcclDataMap = new HashMap<String, List<Number>>(
			3);
	protected static HashMap<String, XYSeriesShimmer> mPlotAcclSeriesMap = new HashMap<String, XYSeriesShimmer>(
			3);
	protected static HashMap<String, List<Number>> mPlotGyroDataMap = new HashMap<String, List<Number>>(
			3);
	protected static HashMap<String, XYSeriesShimmer> mPlotGyroSeriesMap = new HashMap<String, XYSeriesShimmer>(
			3);

	protected static XYPlot dynamicPlot_accl_realtime;
	protected static XYPlot dynamicPlot_gyro_realtime;

	private final static String logFileAppendix = ".csv";
	private final static String logFilePath = Environment
			.getExternalStorageDirectory() + "/ShimmerTest/";
	private static String wholeLogFileName;
	private static File wholeLogFile;
	private static BufferedWriter wholeBuf;
	private static boolean isWholeLoggingOn = false;

	protected static double[] threshData;
	protected final static double threshAcclSdMax = 1;
	protected final static double threshAcclMeanMax = 1.2;
	protected final static double threshGyroSdMax = 0.3;
	protected final static double threshGyroMeanMax = 0.3;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "On Create");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main_recog);

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Log.d(TAG, "Bluetooth Adapter not Enabled!");
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
		}
		if (!isMyServiceRunning()) {
			Log.d(TAG, "MyShimmerService is not Running!");
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
		}
		Log.d(TAG, "onCreate2");
		doBindService();

		threshData = obtainThresholdData();
		samplingRate = getSamplingRate();

		dynamicPlot_accl_realtime = (XYPlot) findViewById(R.id.dynamicPlot_accl_realtime);
		dynamicPlot_accl_realtime.setTitle("Accl Data Plot");
		dynamicPlot_accl_realtime.setBackgroundColor(Color.TRANSPARENT);
		dynamicPlot_accl_realtime.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("0"));

		dynamicPlot_gyro_realtime = (XYPlot) findViewById(R.id.dynamicPlot_gyro_realtime);
		dynamicPlot_gyro_realtime.setTitle("Gyro Data Plot");
		dynamicPlot_gyro_realtime.setBackgroundColor(Color.TRANSPARENT);
		dynamicPlot_gyro_realtime.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("0"));
	}

	@Override
	public void onStart() {
		super.onStart();
		if (mService.isStreamming() == false) {
			Log.d(TAG, "MyShimmerService is not Streamming!");
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
		}
	}

	public void onDestroy() {
		super.onDestroy();

		Log.d(TAG, "onDestroy");
		isWholeLoggingOn = false;
		try {
			if (wholeBuf != null)
				wholeBuf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onPause() {
		super.onPause();

		Log.d(TAG, "onPause");
		mPlotAcclSeriesMap.clear();
		mPlotAcclDataMap.clear();
		mPlotGyroSeriesMap.clear();
		mPlotGyroDataMap.clear();

		dynamicPlot_accl_realtime.clear();
		dynamicPlot_gyro_realtime.clear();

		isWholeLoggingOn = false;
		try {
			if (wholeBuf != null)
				wholeBuf.close();
			Log.d(TAG, "log whole to:" + wholeLogFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onResume() {
		super.onResume();

		Log.d(TAG, "onResume");
		mPlotAcclSeriesMap.clear();
		mPlotAcclDataMap.clear();
		mPlotGyroSeriesMap.clear();
		mPlotGyroDataMap.clear();

		dynamicPlot_accl_realtime.clear();
		dynamicPlot_gyro_realtime.clear();
		// dynamicPlot_accl.clear();
		// dynamicPlot_gyro.clear();

		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			Log.d(TAG, "Bluetooth Adapter not Enabled!");
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
		}
		if (!isMyServiceRunning()) {
			Log.d(TAG, "MyShimmerService is not Running!");
			Intent intent = new Intent();
			setResult(Activity.RESULT_CANCELED, intent);

			finish();
		}
		doBindService();

		wholeLogFileName = "wholelog" + System.currentTimeMillis()
				+ logFileAppendix;
		wholeLogFile = new File(logFilePath, wholeLogFileName);

		if (!wholeLogFile.exists()) {
			try {
				wholeLogFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			wholeBuf = new BufferedWriter(new FileWriter(wholeLogFile, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		isWholeLoggingOn = true;
	}

	protected static void log(double[] input) {
		if (isWholeLoggingOn && input != null && input.length == 6) {

			String str = Math.round(System.currentTimeMillis() % 100000) + ","
					+ String.format("%.2f", input[0]).toString() + ","
					+ String.format("%.2f", input[1]).toString() + ","
					+ String.format("%.2f", input[2]).toString() + ","
					+ String.format("%.2f", input[3]).toString() + ","
					+ String.format("%.2f", input[4]).toString() + ","
					+ String.format("%.2f", input[5]).toString();
			Log.d(TAG, "logging:" + str);

			if (wholeBuf != null) {
				try {
					wholeBuf.append(str);
					wholeBuf.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static void log(String input) {
		if (isWholeLoggingOn && input != null) {
			Log.d(TAG, "logging:" + input);

			if (wholeBuf != null) {
				try {
					wholeBuf.append(input);
					wholeBuf.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected static void logWindow(MyShimmerDataList data) {

		if (data != null && !data.isEmpty()) {

			String LogFileName = "windowlog_" + System.currentTimeMillis()
					+ logFileAppendix;

			File LogFile = new File(logFilePath, LogFileName);
			Log.d(TAG, "log window to:" + LogFile.getAbsolutePath());
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(LogFile,
						true));
				for (int i = 0; i < data.size(); i++) {
					buf.append(data.getSingleInString(i));
					buf.append("\n");
				}
				buf.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	protected static boolean isWindowPositiveForSignal(MyShimmerDataList datas) {
		int diffCount = 0;
		int dataSize = 0;

		if (datas != null && !datas.isEmpty()) {
			dataSize = datas.size();
			Log.d(TAG, "dataSize:" + dataSize);

			double[] sums = new double[6];
			double[] means = new double[6];
			double[] vars = new double[6];
			double[] sds = new double[6];

			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < dataSize; j++) {
					sums[i] += datas.getSerie(i).get(j);
				}

				means[i] += sums[i] / dataSize;
				for (int j = 0; j < dataSize; j++) {
					vars[i] += (means[i] - datas.getSerie(i).get(j))
							* (means[i] - datas.getSerie(i).get(j));
 				}
				vars[i] = vars[i] / dataSize;
				sds[i] = Math.sqrt(vars[i]);
			}

			Log.d(TAG, "mean accl: " + means[0] + ";" + means[1] + ";"
					+ means[2]);
			Log.d(TAG, "mean gyro: " + means[3] + ";" + means[4] + ";"
					+ means[5]);
			Log.d(TAG, "sd accl: " + sds[0] + ";" + sds[1] + ";" + sds[2]);
			Log.d(TAG, "sd gyro: " + sds[3] + ";" + sds[4] + ";" + sds[5]);

			// for (int i = 0; i < 3; i++) {
			// if (Math.abs((means[i] - threshData[i]) / threshData[i]) >=
			// threshAcclMeanMax) {
			// diffCount++;
			// break;
			// }
			// }
			// for (int i = 3; i < 6; i++) {
			// if (Math.abs((means[i] - threshData[i]) / threshData[i]) >=
			// threshGyroMeanMax) {
			// diffCount++;
			// break;
			// }
			// }
			for (int i = 0; i < 6; i++) {
				if (sds[i] >= threshAcclSdMax) {
					diffCount++;
					break;
				}
			}

			Log.d(TAG, "diffCount:" + diffCount);
		}
		return (diffCount > 0 ? true : false);
	}

	private double[] obtainThresholdData() {
		Log.d(TAG, "obtainThresholdData");
		Bundle extras = getIntent().getExtras();
		double[] tmpData = null;
		if (extras != null) {
			tmpData = extras.getDoubleArray(Extra_Prep_data);
		}

		return tmpData;

	}

	private double getSamplingRate() {
		Log.d(TAG, "obtainThresholdData");
		Bundle extras = getIntent().getExtras();
		double tmpData = 0;
		if (extras != null) {
			tmpData = extras.getDouble(Extra_Sampling_Rate);
		}

		return tmpData;
	}

	protected static void calcFeatures(MyShimmerDataList input) {

		if (input != null && !input.isEmpty()) {
			Log.d(TAG, "calcFeatures");
			FeatureClass feature;
			if (samplingRate != 0) {
				feature = new Features(input, samplingRate).getFeatures();
			} else {
				feature = new Features(input, 128).getFeatures();
			}
			Log.d(TAG, feature.toString());

		}
	}
}
