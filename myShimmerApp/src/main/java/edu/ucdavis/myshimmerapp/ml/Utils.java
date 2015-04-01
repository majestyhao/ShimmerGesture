package edu.ucdavis.myshimmerapp.ml;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;

public class Utils {

	private final static String TAG = "MyShimmerApp.Utils";

	public static IirFilterCoefficients getFilterCoef(
			FilterPassType filter_type, int filterOrder, double fcf1,
			double fcf2) {
		// Log.d(TAG, "getFilterCoef:" + filter_type + "," + fcf1 + "," + fcf2);
		IirFilterCoefficients coef = IirFilterDesignFisher.design(filter_type,
				FilterCharacteristicsType.butterworth, filterOrder, 0, fcf1,
				fcf2);

//		{
//			for (double d : coef.a) {
//				Log.d(TAG, "a:" + d);
//			}
//			for (double d : coef.b) {
//				Log.d(TAG, "b:" + d);
//			}
//		}

		return coef;
	}

	public static void logTimeDom(double[] input, String name) {
		if (input != null) {

			String LogFileName = name + ".csv";
			String logFilePath = Environment.getExternalStorageDirectory()
					+ "/ShimmerTest/Logs/";
			File LogFile = new File(logFilePath, LogFileName);
			Log.d(TAG, LogFileName);
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(LogFile,
						true));
				for (int i = 0; i < input.length; i++) {
					buf.append(String.valueOf(input[i]));
					buf.append("\n");
				}
				buf.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	public static void logFFT(FastFourierTransformer fft, double[] input,
			String name) {
		if (input != null) {
			Complex[] data_fft = doFFT(fft, input);
			double[] tmp = new double[data_fft.length];
			for (int i = 0; i < data_fft.length; i++) {
				tmp[i] = 20 * FastMath.log10(data_fft[i].abs());
			}
			String LogFileName = name + ".csv";
			String logFilePath = Environment.getExternalStorageDirectory()
					+ "/ShimmerTest/Logs/";
			File LogFile = new File(logFilePath, LogFileName);
			try {
				BufferedWriter buf = new BufferedWriter(new FileWriter(LogFile,
						true));
				for (int i = 0; i < tmp.length; i++) {
					buf.append(String.valueOf(tmp[i]));
					buf.append("\n");
				}
				buf.close();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	public static double[] filtfilt(IirFilterCoefficients coef, double[] input) {
		double[] ret = null;

		if (input != null && input.length != 0) {
			ret = new double[input.length];
			
			IirFilter filter = new IirFilter(coef);

			for (int i = 0; i < input.length; i++) {
				ret[i] = filter.step(input[i]);
			}

//			DigitalFilter filter = new DigitalFilter(coef.b, coef.a, input);
//			ret = filter.zeroFilter();
		}
		return ret;
	}

	public static int getEnergy(FastFourierTransformer fft, double[] input) {
		// output not same when padding needed
		int ret = 0;
		if (input != null && input.length != 0) {
			Complex[] data_fft = doFFT(fft, input);
			for (int i = 1; i < data_fft.length / 2 + 1; i++) {
				ret += FastMath.pow(data_fft[i].abs(), 2);
			}
			ret = ret / (data_fft.length / 2);
		}
		return ret;
	}

	public static Complex[] doFFT(FastFourierTransformer fft, double[] input) {
		Complex[] output = null;
		double[] data = null;
		if (input != null && input.length != 0) {
			int length = input.length;
			if ((length & (length - 1)) != 0) {// padding if input length is not
												// power of two
				int newlength = Integer.highestOneBit(length - 1) << 1;
				data = new double[newlength];
				for (int i = 0; i < length; i++) {
					data[i] = input[i];
				}
			} else {
				data = input;
			}
			output = new Complex[data.length];
			output = fft.transform(data, TransformType.FORWARD);
		}

		return output;
	}

	public static double fastSqrt(double x, double y, double z) {
		final int expX = FastMath.getExponent(x);
		final int expY = FastMath.getExponent(y);
		final int expZ = FastMath.getExponent(z);
		if ((expY > expX + 27) || (expZ > expX + 27)) {
			return FastMath.hypot(y, z);
		} else if (expX > expY + 27 || expZ > expY + 27) {
			return FastMath.hypot(x, z);
		} else if (expX > expZ + 27 || expY > expZ + 27) {
			return FastMath.hypot(x, y);
		} else {

			// find an intermediate scale to avoid both overflow and underflow
			final int middleExp = (expX + expY + expZ) / 3;

			// scale parameters without losing precision
			final double scaledX = FastMath.scalb(x, -middleExp);
			final double scaledY = FastMath.scalb(y, -middleExp);
			final double scaledZ = FastMath.scalb(z, -middleExp);

			// compute scaled hypotenuse
			final double scaledH = FastMath.sqrt(scaledX * scaledX + scaledY
					* scaledY + scaledZ * scaledZ);

			// remove scaling
			return FastMath.scalb(scaledH, middleExp);

		}

	}

}
