package edu.ucdavis.myshimmerapp.activities;

import com.shimmerresearch.driver.FormatCluster;
import com.shimmerresearch.driver.ObjectCluster;

import org.apache.commons.math3.util.DoubleArray;
import org.apache.commons.math3.util.ResizableDoubleArray;

import java.util.Collection;

public class MyShimmerDataList {

	private final static int Serie_ACCL_X = 0;
	private final static int Serie_ACCL_Y = 1;
	private final static int Serie_ACCL_Z = 2;
	private final static int Serie_GYRO_X = 3;
	private final static int Serie_GYRO_Y = 4;
	private final static int Serie_GYRO_Z = 5;
	private final static int Serie_TimeStamp = 6;

	private final static int Serie_Max = 7;

	DoubleArray timestamp = new ResizableDoubleArray();
	DoubleArray acclX = new ResizableDoubleArray();
	DoubleArray acclY = new ResizableDoubleArray();
	DoubleArray acclZ = new ResizableDoubleArray();
	DoubleArray gyroX = new ResizableDoubleArray();
	DoubleArray gyroY = new ResizableDoubleArray();
	DoubleArray gyroZ = new ResizableDoubleArray();

	public double[] getSingle(int i) {
		double[] ret = new double[Serie_Max];
		ret[Serie_ACCL_X] = acclX.getElement(i);
		ret[Serie_ACCL_Y] = acclY.getElement(i);
		ret[Serie_ACCL_Z] = acclZ.getElement(i);
		ret[Serie_GYRO_X] = gyroX.getElement(i);
		ret[Serie_GYRO_Y] = gyroY.getElement(i);
		ret[Serie_GYRO_Z] = gyroZ.getElement(i);
		ret[Serie_TimeStamp] = timestamp.getElement(i);
		return ret;
	}

	public String getSingleInString(int i) {
		String ret = Math.round(timestamp.getElement(i)) + ","
				+ String.format("%.2f", acclX.getElement(i)).toString() + ","
				+ String.format("%.2f", acclY.getElement(i)).toString() + ","
				+ String.format("%.2f", acclZ.getElement(i)).toString() + ","
				+ String.format("%.2f", gyroX.getElement(i)).toString() + ","
				+ String.format("%.2f", gyroY.getElement(i)).toString() + ","
				+ String.format("%.2f", gyroZ.getElement(i)).toString();
		return ret;
	}

	public double[] getSerie(int i) {
		double[] ret = null;
		switch (i) {
		case Serie_ACCL_X:
			ret = acclX.getElements();
			break;
		case Serie_ACCL_Y:
			ret = acclY.getElements();
			break;
		case Serie_ACCL_Z:
			ret = acclZ.getElements();
			break;
		case Serie_GYRO_X:
			ret = gyroX.getElements();
			break;
		case Serie_GYRO_Y:
			ret = gyroY.getElements();
			break;
		case Serie_GYRO_Z:
			ret = gyroZ.getElements();
			break;
		case Serie_TimeStamp:
			ret = timestamp.getElements();
			break;
		}
		return ret;
	}

	public void copySingle(double[] input) {
		if (input != null && input.length == Serie_Max) {
			acclX.addElement(input[Serie_ACCL_X]);
			acclY.addElement(input[Serie_ACCL_Y]);
			acclZ.addElement(input[Serie_ACCL_Z]);
			gyroX.addElement(input[Serie_GYRO_X]);
			gyroY.addElement(input[Serie_GYRO_Y]);
			gyroZ.addElement(input[Serie_GYRO_Z]);
			timestamp.addElement(input[Serie_TimeStamp]);
		}
	}

	public void add(double[] input) {
		if (input != null && input.length == Serie_Max - 1) {
			timestamp.addElement((double) Math.round((System
					.currentTimeMillis() % 100000)));
			acclX.addElement(input[Serie_ACCL_X]);
			acclY.addElement(input[Serie_ACCL_Y]);
			acclZ.addElement(input[Serie_ACCL_Z]);
			gyroX.addElement(input[Serie_GYRO_X]);
			gyroY.addElement(input[Serie_GYRO_Y]);
			gyroZ.addElement(input[Serie_GYRO_Z]);
		}
	}

	public void addAll(MyShimmerDataList list) {
		if (list != null && !list.isEmpty()) {
			timestamp.addElements(list.getSerie(Serie_TimeStamp));
			acclX.addElements(list.getSerie(Serie_ACCL_X));
			acclY.addElements(list.getSerie(Serie_ACCL_Y));
			acclZ.addElements(list.getSerie(Serie_ACCL_Z));
			gyroX.addElements(list.getSerie(Serie_GYRO_X));
			gyroY.addElements(list.getSerie(Serie_GYRO_Y));
			gyroZ.addElements(list.getSerie(Serie_GYRO_Z));
		}
	}

	public void clear() {
		timestamp.clear();
		acclX.clear();
		acclY.clear();
		acclZ.clear();
		gyroX.clear();
		gyroY.clear();
		gyroZ.clear();
	}

	public int size() {
		return timestamp.getNumElements();
	}

	public boolean isEmpty() {
		return size() == 0 ? true : false;
	}

	private void addSerie(int i, double[] input) {
		if (input != null && input.length != 0) {
			switch (i) {
			case Serie_ACCL_X:
				acclX.addElements(input);
				break;
			case Serie_ACCL_Y:
				acclY.addElements(input);
				break;
			case Serie_ACCL_Z:
				acclZ.addElements(input);
				break;
			case Serie_GYRO_X:
				gyroX.addElements(input);
				break;
			case Serie_GYRO_Y:
				gyroY.addElements(input);
				break;
			case Serie_GYRO_Z:
				gyroZ.addElements(input);
				break;
			case Serie_TimeStamp:
				timestamp.addElements(input);
				break;
			}
		}
	}

	/*
	 * start position inclusive; end position exclusive
	 */
	public static MyShimmerDataList subList(MyShimmerDataList input,
			int startPos, int endPos) {
		MyShimmerDataList ret = new MyShimmerDataList();

		if (input != null && !input.isEmpty()) {
			for (int j = startPos; j < endPos; j++) {
				ret.copySingle(input.getSingle(j));
			}
		}
		return ret;
	}

	public static double[] parseShimmerObject(ObjectCluster input) {

		String[] sensorName = new String[6];
		sensorName[0] = "Accelerometer X";
		sensorName[1] = "Accelerometer Y";
		sensorName[2] = "Accelerometer Z";
		sensorName[3] = "Gyroscope X";
		sensorName[4] = "Gyroscope Y";
		sensorName[5] = "Gyroscope Z";

		double[] data = new double[6];

		for (int i = 0; i < sensorName.length; i++) {
			Collection<FormatCluster> ofFormats = input.mPropertyCluster
					.get(sensorName[i]);
			FormatCluster formatCluster = ((FormatCluster) ObjectCluster
					.returnFormatCluster(ofFormats, "CAL"));
			if (formatCluster != null) {
				data[i] = formatCluster.mData;
			}
		}
		return data;
	}
}
