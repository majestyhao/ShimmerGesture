package edu.ucdavis.myshimmerapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.shimmerresearch.android.Shimmer;
import com.shimmerresearch.driver.ObjectCluster;

import java.util.ArrayList;

public class MyShimmerService extends Service {
	private static final String TAG = "MyShimmerService";

	public static final int Message_ShimmerRead = 1;
	public static final int Message_ShimmerStatusChange = 2;
	public static final int Message_PrepDataDone = 3;
	public static final int Message_TimerCallBack = 4;
	public static final int Message_ServiceConnected = 100;

	private Shimmer shimmerDevice = null;
	private static boolean isStreaming = false;
	private static boolean isDeviceConncted = false;

	private static ArrayList<Handler> mGraphHandler = new ArrayList<Handler>();

	private String mBluetoothAddress = null;

	private final IBinder mBinder = new LocalBinder();

	private static Context context;
	public final static Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case Shimmer.MESSAGE_STATE_CHANGE:
				Log.d(TAG, "Shimmer Status Change" + msg.arg1);
				if (!mGraphHandler.isEmpty()) {
					for (Handler handler : mGraphHandler) {
						handler.obtainMessage(Message_ShimmerStatusChange,
								msg.arg1, -1, msg.obj).sendToTarget();
					}
				}

				switch (msg.arg1) {
				case Shimmer.STATE_CONNECTED:
					isDeviceConncted = true;
					break;

				case Shimmer.MSG_STATE_FULLY_INITIALIZED:
					break;

				case Shimmer.STATE_CONNECTING:
					break;

				case Shimmer.MSG_STATE_STREAMING:
					isStreaming = true;
					break;

				case Shimmer.MSG_STATE_STOP_STREAMING:
					isStreaming = false;
					break;

				case Shimmer.STATE_NONE:
					isDeviceConncted = false;
					isStreaming = false;
					break;
				}
				break;

			case Shimmer.MESSAGE_READ:
				if ((msg.obj instanceof ObjectCluster)) {
					if (!mGraphHandler.isEmpty()) {
						for (Handler handler : mGraphHandler) {
							if (handler != null)
								handler.obtainMessage(Message_ShimmerRead,
										msg.arg1, -1, msg.obj).sendToTarget();
						}
					}
				}
				break;

			case MyShimmerService.Message_ServiceConnected:
				Log.d(TAG, "Message_ServiceConnected");
				if (!mGraphHandler.isEmpty()) {
					for (Handler handler : mGraphHandler) {
						if (handler != null)
							handler.obtainMessage(Message_ServiceConnected,
									msg.arg1, -1, null).sendToTarget();
					}
				}
				break;

			case Shimmer.MESSAGE_ACK_RECEIVED:
				break;

			case Shimmer.MESSAGE_DEVICE_NAME:
				break;

			case Shimmer.MESSAGE_TOAST:
				if (context != null)
					Toast.makeText(context,
							msg.getData().getString(Shimmer.TOAST),
							Toast.LENGTH_SHORT).show();
				break;

			case Shimmer.MESSAGE_LOG_AND_STREAM_STATUS_CHANGED:
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "Service onBind");
		return mBinder;
	}

	public boolean onUnbind(Intent intent) {
		Log.d(TAG, "Service onUnBind");
		return false;
	}

	public void onRebind(Intent intent) {
		Log.d(TAG, "Service onReBind");
	}

	public class LocalBinder extends Binder {
		public MyShimmerService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return MyShimmerService.this;
		}
	}

	public void onCreate() {
		Log.d(TAG, "Service onCreate");
		context = getApplicationContext();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand ");
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	public void onStart(Intent intent, int startid) {
		Log.d(TAG, "Service onStart");
	}

	public void onDestroy() {
		Log.d(TAG, "Service onDestroy");
		if (shimmerDevice != null) {
			shimmerDevice.stop();
		}
	}

	public void onStop() {
		Log.d(TAG, "Service onStop");
		if (shimmerDevice != null) {
			shimmerDevice.stop();
		}
	}

	public void registerGraphHandler(Handler handler) {
		if (handler != null && mGraphHandler != null
				&& !mGraphHandler.contains(handler))
			mGraphHandler.add(handler);
	}

	public void deRegisterGraphHandler(Handler handler) {
		if (handler != null && mGraphHandler != null
				&& mGraphHandler.contains(handler))
			mGraphHandler.remove(handler);
	}

	public void connectShimmer(String bluetoothAddress) {

		mBluetoothAddress = bluetoothAddress;

		shimmerDevice = new Shimmer(this, mHandler, "MyShimmerDevice", 128, Shimmer.SENSOR_ACCEL | Shimmer.SENSOR_GYRO);
		shimmerDevice.connect(mBluetoothAddress, "default");
		Log.d(TAG, "connect shimmer:" + shimmerDevice + " to "
				+ mBluetoothAddress);

	}

	public void discconnectShimmer() {
		if (shimmerDevice != null) {
			Log.d(TAG, "disconnect shimmer");
			shimmerDevice.stop();
		}
	}

	public void startStreaming() {
		if (shimmerDevice != null) {
			Log.d(TAG, "start streaming");
			shimmerDevice.startStreaming();
		}
	}

	public void stopStreaming() {
		if (shimmerDevice != null) {
			Log.d(TAG, "stop streaming");
			shimmerDevice.stopStreaming();
		}
	}

	public boolean isStreamming() {
		return isStreaming;
	}

	public boolean isDeviceConnected() {
		return isDeviceConncted;
	}

	public void enableShimmerSensor(int sensor) {
		if (shimmerDevice != null) {
			Log.d(TAG, "enableShimmerSensor");
			shimmerDevice.writeEnabledSensors(sensor);
		}
	}

	public double getShimmerSampleRate() {
		if (shimmerDevice != null) {
			Log.d(TAG, "getShimmerSampleRate");
			return shimmerDevice.getSamplingRate();
		}
		return 0;
	}

	public void setShimmerSampleRate(double rate) {
		if (shimmerDevice != null) {
			Log.d(TAG, "setShimmerSampleRate:" + rate);
			shimmerDevice.writeSamplingRate(rate);
		}
	}

}
