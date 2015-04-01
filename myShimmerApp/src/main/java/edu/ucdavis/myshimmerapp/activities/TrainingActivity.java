package edu.ucdavis.myshimmerapp.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myshimmerapp.R;
import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import edu.ucdavis.myshimmerapp.ml.GestureNames;
import edu.ucdavis.myshimmerapp.ml.Model;
import edu.ucdavis.myshimmerapp.services.MyShimmerService;

public class TrainingActivity extends RecogTrainActivityBase {

	private static final String TAG = "MyShimmerApp.TrainingActivity";

	private static Button startButton;
	private Button buildButton;
	private static TextView gestureText;

	private static boolean isListening = false;
	private static int count = 0;
	private final static int countMax = 10;
	private static int gest_count = 0;

	private static MyShimmerDataList mWindowData = new MyShimmerDataList();
	private static MyShimmerDataList mWindowDataBak = new MyShimmerDataList();

	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.main_train);
		super.onCreate(savedInstanceState);

		model = new Model(mlAlgo, gestureType, true);

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

		buildButton = (Button) findViewById(R.id.button_build);
		buildButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isListening == false) {
					startButton.setEnabled(false);
					buildButton.setEnabled(false);
					new BuildThread().execute();
				}
			}

		});

		gestureText = (TextView) findViewById(R.id.gesture_name);
		gestureText.setText(GestureNames.types[gestureType][gest_count] + ":"
				+ String.valueOf(count + 1));
		gest_count = 0;
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
							// Log.d(TAG, "isDetected:" + isDetected);

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
									mEndingPoint = 0;
									isListening = false;
									startButton.setEnabled(true);

									Log.d(TAG,
											"******** End Recording Max Time Reached ********");
									Log.d(TAG, "mRecordData.size():"
											+ mRecordData.size());

									MyShimmerDataList toMatchData = mRecordData;
									
									/*
									 * add instance for training, display
									 * gesture name
									 */
									model.addInstanceForTraining(
											calcFeatures(toMatchData, true),
											GestureNames.types[gestureType][gest_count]);

									if (++count >= countMax) {
										if (++gest_count == GestureNames.types[gestureType].length) {
											gest_count = 0;
											gestureText.setText("Done!");
										}
										count = 0;
									}
									gestureText
											.setText(GestureNames.types[gestureType][gest_count]
													+ ":"
													+ String.valueOf(count + 1));
									
									logWindow(toMatchData);

								}
								// } else {
								//
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
								// isListening = false;
								// startButton.setEnabled(true);
								//
								// Log.d(TAG, "mRecordData.size():"
								// + mRecordData.size()
								// + ",mEndingPoint"
								// + mEndingPoint);
								//
								// if (mRecordData.size() >=
								// minRecordWindowSize) {
								// /*
								// * end with one extra following
								// * window.
								// */
								// Log.d(TAG,
								// "******** End Recording ********");
								// MyShimmerDataList toMatchData =
								// MyShimmerDataList
								// .subList(
								// mRecordData,
								// 0,
								// mEndingPoint
								// + mWindowSize);
								//
								// logWindow(toMatchData);
								//
								// /*
								// * add instance for training,
								// * display gesture name
								// */
								// model.addInstanceForTraining(
								// calcFeatures(toMatchData,
								// true),
								// GestureNames.types[gestureType][gest_count]);
								//
								// if (++count >= countMax) {
								// if (++gest_count ==
								// GestureNames.types[gestureType].length) {
								// gest_count = 0;
								// gestureText
								// .setText("Done!");
								// }
								// count = 0;
								// }
								// gestureText
								// .setText(GestureNames.types[gestureType][gest_count]
								// + ":"
								// + String.valueOf(count + 1));
								//
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
		count = 0;
		gest_count = 0;
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
		count = 0;
		gest_count = 0;
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

		isListening = false;
		count = 0;
		gest_count = 0;

		startButton.setEnabled(true);
		buildButton.setEnabled(true);
	}

	private class BuildThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			return model.buildClassfiers();
		}

		protected void onPostExecute(Boolean b) {
			try {
				if (b) {
					Toast.makeText(getApplicationContext(), "Build Done..",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "Build Failed..",
							Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			buildButton.setEnabled(true);
			startButton.setEnabled(true);
		}
	}
}
