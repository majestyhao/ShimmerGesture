package edu.ucdavis.myshimmerapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.myshimmerapp.R;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.myshimmerapp.ml.GestureNames;
import edu.ucdavis.myshimmerapp.ml.Model;
import edu.ucdavis.myshimmerapp.services.MyShimmerService;

public class RecogIntermittentActivity extends RecogTrainActivityBase {

	private static final String TAG = "MyShimmerApp.RecogIntermittentActivity";

	public final static String Extra_Window_Sizes = "Extra_WindowSizes";

	private static Button startButton;
	private static boolean isListening = false;

	private static int mWrapWindowCounter = 0;
	private final static int mWrapWindowMax = 4;

	private static List<ObjectCluster> mWindowData = new ArrayList<ObjectCluster>();
	private static List<ObjectCluster> mWrapWindowData = new ArrayList<ObjectCluster>();

	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_recog);
		super.onCreate(savedInstanceState);

		model = new Model(mlAlgo, gestureType, false);
		if (!model.isInitializedforValidation()) {
			Log.d(TAG, "No Model File Exist! Exit Matching!");
			finish();
		}

		startButton = (Button) findViewById(R.id.button_start);
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isListening) {

					startButton.setEnabled(false);
					isListening = true;
				}
			}

		});
	}

	public static Handler mActivityHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MyShimmerService.Message_ShimmerStatusChange:
				Log.d(TAG, "State Change:" + msg.arg1);
				switch (msg.arg1) {
				case Shimmer.MSG_STATE_STOP_STREAMING:
				case Shimmer.STATE_NONE:
					break;
				}
				break;

			case MyShimmerService.Message_ServiceConnected:
				Log.d(TAG, "Message_ServiceConnected");
				break;

			case MyShimmerService.Message_ShimmerRead:
				if (isListening) {
					if (msg.obj instanceof ObjectCluster) {
						ObjectCluster data = (ObjectCluster) msg.obj;

						// test begin
						{
							double[] datatmp = MyShimmerDataList
									.parseShimmerObject(data);
							log(datatmp);
							if (mIsRecording) {
								drawGraph(dynamicPlot_accl_realtime,
										mPlotAcclDataMap, mPlotAcclSeriesMap,
										SENSOR_TYPE_ACCL, datatmp);
								drawGraph(dynamicPlot_gyro_realtime,
										mPlotGyroDataMap, mPlotGyroSeriesMap,
										SENSOR_TYPE_GYRO, datatmp);
							}
						}
						// test end

						mWindowData.add(data);

						if (++mWindowCounter >= mWindowSize) {
							boolean isDetected = false;
							if (mIsRecording == false) {
								mWrapWindowData.addAll(mWindowData);

								/* intermittently inspect while not recording */
								if (++mWrapWindowCounter >= mWrapWindowMax) {

									isDetected = isWindowPositiveForSignal(convertShimmerDataList(mWindowData));
									Log.d(TAG, "isDetected:" + isDetected);

									if (isDetected) {
										mRecordData.clear();

										Log.d(TAG,
												"******** Start Recording ********");
										mIsRecording = true;
										mRecordData = addRecordData(convertShimmerDataList(mWrapWindowData));
										mEndingPoint = mRecordData.size();
									}

									mWrapWindowData.clear();
									mWrapWindowCounter = 0;
								}
							} else {
								/* inspect every window while recording */

								isDetected = isWindowPositiveForSignal(convertShimmerDataList(mWindowData));
								Log.d(TAG, "isDetected:" + isDetected);

								mRecordData
										.addAll(convertShimmerDataList(mWindowData));

								if (mRecordData.size() >= maxRecordWindowSize) {
									mIsRecording = false;
									mEndingWindowCounter = 0;
									startButton.setEnabled(true);
									isListening = false;

									Log.d(TAG,
											"******** End Recording Max Time Reached ********");
									Log.d(TAG, "mRecordData.size():"
											+ mRecordData.size());

									MyShimmerDataList toMatchData = mRecordData;


									/**
									 * calculate features, matching trained
									 * models, and display result.
									 **/
									int type = model.classify(calcFeatures(
											toMatchData, false));
									if (type >= 0
											&& type < GestureNames.types[gestureType].length) {
										resultText
												.setText(GestureNames.types[gestureType][type]);
									}
									
									logWindow(toMatchData);
								}

								// if (!isDetected) {
								// mEndingWindowCounter++;
								// } else {
								// mEndingWindowCounter = 0;// mEndingWindowMax
								// // consecutive
								// // non-detected
								// // windows is
								// // considered to
								// // be ended.
								// mEndingPoint = mRecordData.size();
								// }
								//
								// if (mEndingWindowCounter == 0) {
								// // mEndingPoint = mRecordData.size();
								// }
								//
								// if (mEndingWindowCounter >= mEndingWindowMax)
								// {
								//
								// mIsRecording = false;
								//
								// mEndingWindowCounter = 0;
								//
								// mWrapWindowCounter = 0;
								// mWrapWindowData.clear();
								//
								// Log.d(TAG,
								// "******** End Recording ********");
								//
								// Log.d(TAG, "mRecordData.size():"
								// + mRecordData.size());
								// /*
								// * end with one extra following window.
								// */
								// if (mRecordData.size() >=
								// minRecordWindowSize) {
								// MyShimmerDataList toMatchData =
								// MyShimmerDataList
								// .subList(mRecordData, 0,
								// mEndingPoint
								// + mWindowSize);
								// logWindow(toMatchData);
								//
								// /**
								// * calculate features, matching trained
								// * models, and display result.
								// **/
								// int type = model.classify(calcFeatures(
								// toMatchData, false));
								// if (type >= 0
								// && type <
								// GestureNames.types[gestureType].length) {
								// resultText
								// .setText(GestureNames.types[gestureType][type]);
								// }
								// }
								// }
							}
							mWindowData.clear();
							mWindowCounter = 0;
						}
					}
				}
				break;
			}
		}
	};

	public void onDestroy() {
		super.onDestroy();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
		
		isListening = false;
	}

	public void onPause() {
		super.onPause();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.deRegisterGraphHandler(mActivityHandler);
		
		isListening = false;
	}

	public void onResume() {
		super.onResume();

		mWindowData.clear();
		mRecordData.clear();
		mWrapWindowData.clear();
		mWrapWindowCounter = 0;
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.registerGraphHandler(mActivityHandler);
		
		startButton.setEnabled(true);
		isListening = false;
	}

	private static MyShimmerDataList convertShimmerDataList(
			List<ObjectCluster> input) {

		MyShimmerDataList ret = new MyShimmerDataList();

		if (input != null && input.size() != 0) {
			for (ObjectCluster obj : input) {
				ret.add(MyShimmerDataList.parseShimmerObject(obj));
			}
		}

		return ret;
	}

	private static MyShimmerDataList addRecordData(MyShimmerDataList tmpAll) {
		MyShimmerDataList ret = new MyShimmerDataList();
		if (tmpAll != null && tmpAll.size() != 0) {

			int dataSize = tmpAll.size();

			Log.d(TAG, "addRecordData:" + dataSize);

			int index = mWrapWindowMax - 1;// default set to the last

			// int lo = 0;
			// int hi = mWrapWindowMax - 2;
			// int mid = (lo + hi) / 2;
			//
			// while (lo <= hi) {
			// Log.d(TAG, "low:" + lo + ";high:" + hi);
			// Log.d(TAG, "low:" + mid * mWindowSize + ";high:" + mWindowSize
			// * (mid + 1));
			// List<MyShimmerData> tmp = tmpAll.subList(mWindowSize * mid,
			// mWindowSize * (mid + 1));
			// if (isWindowPositiveForSignal(tmp)) {
			// hi = mid - 1;
			// index = mid;
			// } else {
			// lo = mid + 1;
			// }
			// mid = (lo + hi) / 2;
			// }

			int lo = 0;
			int hi = mWrapWindowMax - 2;
			while (lo <= hi) {
				Log.d(TAG, "low:" + lo + ";high:" + hi);
				MyShimmerDataList tmp = MyShimmerDataList.subList(tmpAll,
						mWindowSize * lo, mWindowSize * (lo + 1));
				if (isWindowPositiveForSignal(tmp)) {
					index = lo;
				}
				lo++;
			}

			/*
			 * begin with one extra previous window. loses this when 1st window
			 * in wrapping window is positive
			 */
			if (index != 0)
				ret = MyShimmerDataList.subList(tmpAll, (index - 1)
						* mWindowSize, dataSize);
			else
				ret = MyShimmerDataList.subList(tmpAll, 0, dataSize);
			Log.d(TAG, "ret:" + ret.size());
		}
		return ret;
	}
}
