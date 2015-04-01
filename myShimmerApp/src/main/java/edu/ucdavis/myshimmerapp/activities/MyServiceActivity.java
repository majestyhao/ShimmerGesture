package edu.ucdavis.myshimmerapp.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import edu.ucdavis.myshimmerapp.services.MyShimmerService;
import edu.ucdavis.myshimmerapp.services.MyShimmerService.LocalBinder;
import pl.flex_it.androidplot.XYSeriesShimmer;

public class MyServiceActivity extends Activity {
	private static final String TAG = "MyShimmerServiceActivity";
	private final static int X_AXIS_LENGTH = 500;

	public static final int SENSOR_TYPE_ACCL = 1;
	public static final int SENSOR_TYPE_GYRO = 2;

	static MyShimmerService mService = null;
	protected static boolean mServiceBinded = false;

	protected static ServiceConnection mTestServiceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName arg0, IBinder service) {
			Log.d(TAG, "onServiceConnected");
			LocalBinder binder = (MyShimmerService.LocalBinder) service;
			mService = binder.getService();

			mServiceBinded = true;
			// update the view
			MyShimmerService.mHandler.obtainMessage(
					MyShimmerService.Message_ServiceConnected).sendToTarget();
		}

		public void onServiceDisconnected(ComponentName arg0) {
			Log.d(TAG, "onServiceDisconnected");
			mService = null;
			mServiceBinded = false;
		}
	};

	protected boolean isMyServiceRunning() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		Log.d(TAG, MyShimmerService.class.getName());
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (MyShimmerService.class.getName().equals(
					service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	// protected void onPause() {
	//
	// Log.d(TAG, "onPause");
	//
	// doUnbindService();
	// super.onPause();
	// }

	// protected void onDestroy() {
	// super.onDestroy();
	// doUnbindService();
	// }

	protected void doUnbindService() {
		Log.d(TAG, "doUnbindService");
		if (mServiceBinded) {
			Log.d(TAG, "doUnbindService2");
			// Detach our existing connection.
			getApplicationContext().unbindService(mTestServiceConnection);
		}
	}

	protected void doBindService() {
		Log.d(TAG, "doBindService");
		if (!mServiceBinded) {
			Log.d(TAG, "doBindService2");
			getApplicationContext().bindService(
					new Intent(this, MyShimmerService.class),
					mTestServiceConnection, Context.BIND_AUTO_CREATE);
		} else {
			// update the view
			MyShimmerService.mHandler.obtainMessage(
					MyShimmerService.Message_ServiceConnected).sendToTarget();
		}
	}

	protected static Timer startTimer(final String timerName, long delay,
			boolean isRepeat, final Handler handler) {
		if (handler == null || timerName == null) {
			return null;
		}

		Timer timerToStart = new Timer(timerName);
		Log.d(TAG, "******** startTimer ********:" + timerToStart);

		if (isRepeat) {
			Log.d(TAG, "Start Timer Repeat!:" + timerName);
			timerToStart.schedule(new TimerTask() {

				public void run() {
					Log.d(TAG, "******** Timer Expired! ********:" + timerName);
					handler.obtainMessage(
							MyShimmerService.Message_TimerCallBack, timerName)
							.sendToTarget();
				}
			}, delay, delay);

		} else {
			Log.d(TAG, "Start Timer!:" + timerName);
			timerToStart.schedule(new TimerTask() {

				public void run() {
					Log.d(TAG, "******** Timer Expired! ********:" + timerName);
					handler.obtainMessage(
							MyShimmerService.Message_TimerCallBack, timerName)
							.sendToTarget();
				}
			}, delay);
		}
		return timerToStart;
	}

	protected static void cancelTimer(Timer timer) {
		if (timer != null) {
			Log.d(TAG, "******** cancelTimer ********" + timer);
			timer.purge();
			timer.cancel();
		}
		timer = null;
	}

	protected static void drawGraph(XYPlot plot,
			HashMap<String, List<Number>> plotDataMap,
			HashMap<String, XYSeriesShimmer> plotSeriesMap, int sensorType,
			double[] datas) {

		String seriesName[] = new String[3];
		seriesName[0] = "X";
		seriesName[1] = "Y";
		seriesName[2] = "Z";

		int colors[] = new int[3];
		colors[0] = Color.GREEN;
		colors[1] = Color.RED;
		colors[2] = Color.BLUE;

		for (int i = 0; i < 3; i++) {
			List<Number> data;
			if (plotDataMap.get(seriesName[i]) != null) {
				data = plotDataMap.get(seriesName[i]);
			} else {
				data = new ArrayList<Number>();
			}
			if (data.size() > X_AXIS_LENGTH) {
				data.clear();
			}
			if (sensorType == SENSOR_TYPE_ACCL) {
				data.add(datas[i]);
			} else if (sensorType == SENSOR_TYPE_GYRO) {
				data.add(datas[i + 3]);
			}

			plotDataMap.put(seriesName[i], data);
			LineAndPointFormatter lapf = new LineAndPointFormatter(colors[i],
					null, null);
			lapf.getLinePaint().setStrokeWidth(5);
			if (plotSeriesMap.get(seriesName[i]) != null) {
				// if the series exist get the line format
				plotSeriesMap.get(seriesName[i]).updateData(data);
			} else {
				XYSeriesShimmer series = new XYSeriesShimmer(data, 0,
						seriesName[i]);
				plotSeriesMap.put(seriesName[i], series);
				plot.addSeries(plotSeriesMap.get(seriesName[i]), lapf);
			}
		}
		plot.redraw();

	}
}
