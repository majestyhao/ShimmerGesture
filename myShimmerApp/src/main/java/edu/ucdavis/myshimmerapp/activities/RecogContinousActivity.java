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

import edu.ucdavis.myshimmerapp.ml.GestureNames;
import edu.ucdavis.myshimmerapp.ml.Model;
import edu.ucdavis.myshimmerapp.services.MyShimmerService;

public class RecogContinousActivity extends RecogTrainActivityBase {
	private final static String TAG = "MyShimmerApp.RecogContinousActivity";

	private static Button startButton;
	private static boolean isListening = false;

	private static MyShimmerDataList mWindowData = new MyShimmerDataList();
	private static MyShimmerDataList mWindowDataBak = new MyShimmerDataList();

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

	protected static Handler mActivityHandler = new Handler() {
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
					if ((msg.obj instanceof ObjectCluster)) {
						ObjectCluster datas = (ObjectCluster) msg.obj;

						double[] datatmp = MyShimmerDataList
								.parseShimmerObject(datas);

						mWindowData.add(datatmp);

						// test begin
						{
							log(mWindowData.getSingleInString(mWindowData
									.size() - 1));
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

						if (++mWindowCounter >= mWindowSize) {
							boolean isDetected = isWindowPositiveForSignal(mWindowData);
//							Log.d(TAG, "isDetected:" + isDetected);

							if (mIsRecording == false) {
								if (isDetected) {
									Log.d(TAG,
											"******** Start Recording ********");
									mRecordData.clear();
									mEndingWindowCounter = 0;

									mIsRecording = true;

									/*
									 * begin with one extra previous window.
									 */
									mRecordData.addAll(mWindowDataBak);
									mRecordData.addAll(mWindowData);
									mEndingPoint = mRecordData.size();
								} else {
									mWindowDataBak.clear();
									mWindowDataBak.addAll(mWindowData);
								}
							} else {
								mRecordData.addAll(mWindowData);

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
								// } else {
								// if (!isDetected) {
								// mEndingWindowCounter++;
								// } else {
								// mEndingWindowCounter = 0;// mEndingWindowMax
								// // consecutive
								// // non-detected
								// // windows
								// // is
								// // considered
								// // to
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
								// mEndingWindowCounter = 0;
								// startButton.setEnabled(true);
								// isListening = false;
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
								// .subList(
								// mRecordData,
								// 0,
								// mEndingPoint
								// + mWindowSize);
								// logWindow(toMatchData);
								//
								// /**
								// * calculate features, matching
								// * trained models, and display
								// * result.
								// **/
								// int type = model
								// .classify(calcFeatures(
								// toMatchData, false));
								// if (type >= 0
								// && type <
								// GestureNames.types[gestureType].length) {
								// resultText
								// .setText(GestureNames.types[gestureType][type]);
								// }
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

			case MyShimmerService.Message_TimerCallBack:

				if (msg.obj instanceof String) {
					String timerName = (String) msg.obj;
					Log.d(TAG, "Message_TimerCallBack:" + timerName);

				}
				break;
			}
		}
	};

	public void onDestroy() {
		super.onDestroy();

		mWindowData.clear();
		mWindowDataBak.clear();
		mRecordData.clear();
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
		mWindowDataBak.clear();
		mRecordData.clear();
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
		mWindowDataBak.clear();
		mRecordData.clear();
		mWindowCounter = 0;
		mEndingWindowCounter = 0;
		mIsRecording = false;

		if (mService != null)
			mService.registerGraphHandler(mActivityHandler);

		startButton.setEnabled(true);
		isListening = false;
	}

}
