package edu.ucdavis.myshimmerapp.ml;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import biz.source_code.dsp.filter.FilterCharacteristicsType;
import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilter;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import biz.source_code.dsp.filter.IirFilterDesignFisher;
import edu.ucdavis.myshimmerapp.activities.MyShimmerDataList;

public class FeaturesCalc {

	private final static String TAG = "MyShimmerApp.FeatureCalc";

	private double[][] datas;
	private double[][] lowpass;
	private double[][] bandpass;
	private double[][] bandpassAbs;
	private double[][] bandenergy;
	private double[][] lowenergy;
	private double[][] modvigenergy;

	private double samplingRate = 0;
	private final int filterOrder = 2;

	private final double lowPass_Fcf1 = 6.4;
	private final double bandPass_Fcf1 = 0.64;
	private final double bandPass_Fcf2 = 57.6;
	private final double bandEnergy_Fcf1 = 1.92;
	private final double bandEnergy_Fcf2 = 22.4;
	private final double lowEnergy_Fcf1 = 4.48;
	private final double modvigEnergy_Fcf1 = 4.52;
	private final double modvigEnergy_Fcf2 = 57.6;

	private IirFilterCoefficients lowPass_coef;
	private IirFilterCoefficients bandPass_coef;
	private IirFilterCoefficients bandEnergy_coef;
	private IirFilterCoefficients lowEnergy_coef;
	private IirFilterCoefficients modvigEnergy_coef;

	FastFourierTransformer fft = new FastFourierTransformer(
			DftNormalization.STANDARD);
	private Complex[][] data_band_fft = new Complex[6][];

	private DescriptiveStatistics[] stat_data_band = new DescriptiveStatistics[6];
	private DescriptiveStatistics[] stat_data_band_abs = new DescriptiveStatistics[6];

	FeatureClass feature = new FeatureClass();

	public FeaturesCalc(MyShimmerDataList input, double sampleRt) {
		if (input != null && !input.isEmpty()) {

			int dataSize = input.size();
			Log.d(TAG, "construct features:" + dataSize + ",samplingRate:"
					+ sampleRt);

			datas = new double[6][input.size()];
			samplingRate = sampleRt;

			lowPass_coef = getFilterCoef(FilterPassType.lowpass, lowPass_Fcf1
					/ samplingRate, 0);
			bandPass_coef = getFilterCoef(FilterPassType.bandpass,
					bandPass_Fcf1 / samplingRate, bandPass_Fcf2 / samplingRate);
			bandEnergy_coef = getFilterCoef(FilterPassType.bandpass,
					bandEnergy_Fcf1 / samplingRate, bandEnergy_Fcf2
							/ samplingRate);
			lowEnergy_coef = getFilterCoef(FilterPassType.lowpass,
					lowEnergy_Fcf1 / samplingRate, 0);
			modvigEnergy_coef = getFilterCoef(FilterPassType.bandpass,
					modvigEnergy_Fcf1 / samplingRate, modvigEnergy_Fcf2
							/ samplingRate);

			lowpass = new double[6][dataSize];
			bandpass = new double[6][dataSize];
			bandpassAbs = new double[6][dataSize];
			bandenergy = new double[6][dataSize];
			lowenergy = new double[6][dataSize];
			modvigenergy = new double[6][dataSize];

			for (int i = 0; i < 6; i++) {
				double[] serie = input.getSerie(i);
				for (int j = 0; j < input.size(); j++) {
					datas[i][j] = serie[j];
				}
			}

			for (int i = 0; i < 6; i++) {

				lowpass[i] = filtfilt(lowPass_coef, datas[i]);
				bandpass[i] = filtfilt(bandPass_coef, datas[i]);

				bandenergy[i] = filtfilt(bandEnergy_coef, datas[i]);
				lowenergy[i] = filtfilt(lowEnergy_coef, datas[i]);
				modvigenergy[i] = filtfilt(modvigEnergy_coef, datas[i]);

				// logFFT(datas[i], "fft_original_" + i);
				// logFFT(lowpass[i], "fft_lowpass_" + i);
				// logFFT(bandpass[i], "fft_bandpass_" + i);
				// logFFT(bandenergy[i], "fft_bandenergy_" + i);
				// logFFT(lowenergy[i], "fft_lowenergy_" + i);
				// logFFT(modvigenergy[i], "fft_modvienergy_" + i);

				// logTimeDom(lowpass[i], "lowpass_" + i);
				// logTimeDom(bandpass[i], "bandpass_" + i);
				// logTimeDom(bandenergy[i], "bandenergy_" + i);
				// logTimeDom(lowenergy[i], "lowenergy_" + i);
				// logTimeDom(modvigenergy[i], "modvigenergy_" + i);

				// lowpass[i] = MathArrays.copyOf(datas[i]);
				// bandpass[i] = MathArrays.copyOf(datas[i]);
				//
				// bandenergy[i] = MathArrays.copyOf(datas[i]);
				// lowenergy[i] = MathArrays.copyOf(datas[i]);
				// modvigenergy[i] = MathArrays.copyOf(datas[i]);

				for (int j = 0; j < bandpass[i].length; j++) {
					bandpassAbs[i][j] = Math.abs(bandpass[i][j]);
				}
			}

			for (int i = 0; i < 6; i++) {
				stat_data_band[i] = new DescriptiveStatistics(bandpass[i]);
				stat_data_band_abs[i] = new DescriptiveStatistics(
						bandpassAbs[i]);
			}

			calcFeatures();
		}

	}

	public double[] getFeatures() {
		return feature.getSerializedDoubles();
	}

	private IirFilterCoefficients getFilterCoef(FilterPassType filter_type,
			double fcf1, double fcf2) {
		Log.d(TAG, "getFilterCoef:" + filter_type + "," + fcf1 + "," + fcf2);
		IirFilterCoefficients coef = IirFilterDesignFisher.design(filter_type,
				FilterCharacteristicsType.butterworth, filterOrder, 0, fcf1,
				fcf2);

		// {
		// for (double d : coef.a) {
		// Log.d(TAG, "a:" + d);
		// }
		// for (double d : coef.b) {
		// Log.d(TAG, "b:" + d);
		// }
		// }

		return coef;
	}

	private void logTimeDom(double[] input, String name) {
		if (input != null) {

			String LogFileName = name + ".csv";
			String logFilePath = Environment.getExternalStorageDirectory()
					+ "/ShimmerTest/";
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

	private void logFFT(double[] input, String name) {
		if (input != null) {
			Complex[] data_fft = doFFT(input);
			double[] tmp = new double[data_fft.length];
			for (int i = 0; i < data_fft.length; i++) {
				tmp[i] = 20 * FastMath.log10(data_fft[i].abs());
			}
			String LogFileName = name + ".csv";
			String logFilePath = Environment.getExternalStorageDirectory()
					+ "/ShimmerTest/";
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

	private double[] filtfilt(IirFilterCoefficients coef, double[] input) {
		double[] ret = null;

		if (input != null && input.length != 0) {
			// Log.d(TAG, "filtfilt" + input.length);
			ret = new double[input.length];
			IirFilter filter = new IirFilter(coef);

			for (int i = 0; i < input.length; i++) {
				ret[i] = filter.step(input[i]);
			}
		}
		return ret;
	}

	private double getArea(double[] input) {
		double sum = 0;
		if (input != null && input.length != 0) {
			for (double d : input) {
				sum += d;
			}
		}
		return sum;
	}

	private int getEnergy(double[] input) {
		// output not same when padding needed
		int ret = 0;
		if (input != null && input.length != 0) {
			Complex[] data_fft = doFFT(input);
			for (int i = 1; i < data_fft.length / 2 + 1; i++) {
				ret += FastMath.pow(data_fft[i].abs(), 2);
			}
			ret = ret / (data_fft.length / 2);
		}
		return ret;
	}

	private Complex[] doFFT(double[] input) {
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

	private double getBandDataPitch(Complex[] input) {
		// TODO
		double ret = 0;
		if (input != null && input.length != 0) {

		}
		return ret;
	}

	private double getBandDataEntropy(Complex[] input) {
		double ret = 0;
		if (input != null && input.length != 0) {
			double[] abs = new double[input.length];
			double sum = 0;
			double[] norm = new double[input.length];
			double[] norm_log2s = new double[input.length];
			for (int i = 0; i < input.length; i++) {
				abs[i] = input[i].abs();
				sum += abs[i];
			}
			for (int i = 0; i < input.length; i++) {
				norm[i] = abs[i] / sum;
				norm_log2s[i] = FastMath.log(2, norm[i]);
				ret += (norm[i] * norm_log2s[i]);
			}
			ret = ret / input.length;
		}
		return ret;
	}

	private double getBandDataDormFreqRatio(Complex[] input) {
		// TODO not same
		double ret = 0;
		if (input != null && input.length != 0) {
			int length = input.length / 2 + 1;
			double[] abs = new double[length];
			double sum = 0;
			for (int i = 0; i < length; i++) {
				abs[i] = input[i].abs();
				sum += abs[i];
			}
			MathArrays.sortInPlace(abs);
			ret = abs[length - 1] / sum;
		}
		return ret;
	}

	private double getBandDataMCR(double[] input, double mean) {
		double ret = 0;
		if (input != null && input.length != 0) {
			for (int i = 0; i < input.length - 1; i++) {
				if (((input[i] - mean) > 0) != ((input[i + 1] - mean) > 0)) {
					ret++;
				}
			}
			ret = ret / (input.length - 1);
		}
		return ret;
	}

	private double getBandDataTotalSVM(double[] x, double[] y, double[] z) {
		double ret = 0;
		if (x != null && y != null && z != null) {
			double[] sqrts = new double[x.length];
			for (int i = 0; i < x.length; i++) {
				sqrts[i] = fastSqrt(x[i], y[i], z[i]);
			}
			ret = StatUtils.mean(sqrts);
		}
		return ret;
	}

	private double fastSqrt(double x, double y, double z) {
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

	private void calcFeatures() {
		int counter = 0;
		for (int i = 0; i < 6; i++) {
			// low_pass value features
			feature.data_low_area[i] = getArea(lowpass[i]);
			feature.data_low_mean[i] = feature.data_low_area[i]
					/ lowpass[i].length;

			// absolute band_pass value features
			feature.data_band_abs_mean[i] = stat_data_band_abs[i].getMean();
			feature.data_band_abs_area[i] = stat_data_band_abs[i].getSum();
			feature.data_band_abs_std[i] = stat_data_band_abs[i]
					.getStandardDeviation();
			feature.data_band_abs_quantile_25[i] = stat_data_band_abs[i]
					.getPercentile(25);
			feature.data_band_abs_quantile_50[i] = stat_data_band_abs[i]
					.getPercentile(50);
			feature.data_band_abs_quantile_75[i] = stat_data_band_abs[i]
					.getPercentile(75);
			feature.data_band_abs_cv[i] = feature.data_band_abs_std[i]
					/ feature.data_band_abs_mean[i] * 100;
			feature.data_band_IQR[i] = feature.data_band_abs_quantile_75[i]
					- feature.data_band_abs_quantile_25[i];

			// band_pass value features
			feature.data_band_skew[i] = stat_data_band[i].getSkewness();
			feature.data_band_kur[i] = stat_data_band[i].getKurtosis();
			feature.data_band_var[i] = stat_data_band[i].getVariance();
			feature.data_band_range[i] = stat_data_band[i].getMax()
					- stat_data_band[i].getMin();
			feature.data_band_MCR[i] = getBandDataMCR(bandpass[i],
					stat_data_band[i].getMean());

			data_band_fft[i] = doFFT(bandpass[i]);
			feature.data_band_pitch[i] = getBandDataPitch(data_band_fft[i]);
			feature.data_band_dom_freq_ratio[i] = getBandDataDormFreqRatio(data_band_fft[i]);
			feature.data_band_entropy[i] = getBandDataEntropy(data_band_fft[i]);

			// energies
			feature.data_energy[i] = getEnergy(datas[i]);
			feature.data_band_energy[i] = getEnergy(bandenergy[i]);
			feature.data_low_energy[i] = getEnergy(lowenergy[i]);
			feature.data_modvig_energy[i] = getEnergy(modvigenergy[i]);

			{
				feature.feature_all[counter++] = feature.data_low_mean[i];
				feature.feature_all[counter++] = feature.data_low_area[i];
				feature.feature_all[counter++] = feature.data_band_abs_mean[i];
				feature.feature_all[counter++] = feature.data_band_abs_area[i];
				feature.feature_all[counter++] = feature.data_band_entropy[i];
				feature.feature_all[counter++] = feature.data_band_skew[i];
				feature.feature_all[counter++] = feature.data_band_kur[i];
				feature.feature_all[counter++] = feature.data_band_abs_quantile_25[i];
				feature.feature_all[counter++] = feature.data_band_abs_quantile_50[i];
				feature.feature_all[counter++] = feature.data_band_abs_quantile_75[i];
				feature.feature_all[counter++] = feature.data_band_var[i];
				feature.feature_all[counter++] = feature.data_band_abs_cv[i];
				feature.feature_all[counter++] = feature.data_band_IQR[i];
				feature.feature_all[counter++] = feature.data_band_range[i];
				feature.feature_all[counter++] = feature.data_energy[i];
				feature.feature_all[counter++] = feature.data_band_energy[i];
				feature.feature_all[counter++] = feature.data_low_energy[i];
				feature.feature_all[counter++] = feature.data_modvig_energy[i];
				feature.feature_all[counter++] = feature.data_band_pitch[i];
				feature.feature_all[counter++] = feature.data_band_dom_freq_ratio[i];
				feature.feature_all[counter++] = feature.data_band_MCR[i];
			}
		}
		for (int i = 0; i < 2; i++) {
			feature.data_low_total_mean[i] = (feature.data_low_mean[i * 3]
					+ feature.data_low_mean[i * 3 + 1] + feature.data_low_mean[i * 3 + 2]) / 3;

			feature.data_low_posture_dist[i][0] = feature.data_low_mean[i * 3]
					- feature.data_low_mean[i * 3 + 1];
			feature.data_low_posture_dist[i][1] = feature.data_low_mean[i * 3 + 1]
					- feature.data_low_mean[i * 3 + 2];
			feature.data_low_posture_dist[i][2] = feature.data_low_mean[i * 3 + 2]
					- feature.data_low_mean[i * 3];

			feature.data_band_total_abs_area[i] = feature.data_band_abs_area[i * 3]
					+ feature.data_band_abs_area[i * 3 + 1]
					+ feature.data_band_abs_area[i * 3 + 2];

			feature.data_band_total_SVM[i] = getBandDataTotalSVM(
					bandpass[i * 3], bandpass[i * 3 + 1], bandpass[i * 3 + 2]);

			{
				feature.feature_all[counter++] = feature.data_low_total_mean[i];
				feature.feature_all[counter++] = feature.data_low_posture_dist[i][0];
				feature.feature_all[counter++] = feature.data_low_posture_dist[i][1];
				feature.feature_all[counter++] = feature.data_low_posture_dist[i][2];
				feature.feature_all[counter++] = feature.data_band_total_abs_area[i];
				feature.feature_all[counter++] = feature.data_band_total_SVM[i];
			}
		}
	}

	private class FeatureClass {
		public double[] data_low_mean = new double[6];// DCMean
		public double[] data_low_area = new double[6];// DCArea

		public double[] data_band_abs_mean = new double[6];// ACAbsMean
		public double[] data_band_abs_area = new double[6];// ACAbsArea
		public double[] data_band_abs_std = new double[6];
		public double[] data_band_entropy = new double[6];// ACEntropy
		public double[] data_band_skew = new double[6];// ACSkew
		public double[] data_band_kur = new double[6];// ACKur
		public double[] data_band_abs_quantile_25 = new double[6];// ACQuartiles
		public double[] data_band_abs_quantile_50 = new double[6];// ACQuartiles
		public double[] data_band_abs_quantile_75 = new double[6];// ACQuartiles
		public double[] data_band_var = new double[6];// ACVar
		public double[] data_band_abs_cv = new double[6];// ACAbsCV
		public double[] data_band_IQR = new double[6];// ACIQR
		public double[] data_band_range = new double[6];// ACRange
		public double[] data_band_pitch = new double[6];// ACPitch
		public double[] data_band_dom_freq_ratio = new double[6];// ACDomFreqRatio
		public double[] data_band_MCR = new double[6];// ACMCR

		public double[] data_energy = new double[6];// ACEnergy
		public double[] data_band_energy = new double[6];// ACBandEnergy
		public double[] data_low_energy = new double[6];// ACLowEnergy
		public double[] data_modvig_energy = new double[6];// ACModVigEnergy

		public double[] data_low_total_mean = new double[2];// DCTotalMean
		public double[][] data_low_posture_dist = new double[2][3];// DCPostureDist
		public double[] data_band_total_abs_area = new double[2];// ACTotalAbsArea
		public double[] data_band_total_SVM = new double[2];// ACTotalSVM

		public double[] feature_all = new double[21 * 6 + 6 * 2];

		public double[] getSerializedDoubles() {
			return feature_all;
		}

		public String toString() {
			String ret = "DCMean:";
			for (double d : data_low_mean) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n DCArea:";
			for (double d : data_low_area) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACAbsMean:";
			for (double d : data_band_abs_mean) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACAbsArea:";
			for (double d : data_band_abs_area) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n data_band_abs_std:";
			for (double d : data_band_abs_std) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACEntropy:";
			for (double d : data_band_entropy) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACSkew:";
			for (double d : data_band_skew) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACSKur:";
			for (double d : data_band_kur) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACQuartiles_25:";
			for (double d : data_band_abs_quantile_25) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACQuartiles_50:";
			for (double d : data_band_abs_quantile_50) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACQuartiles_75:";
			for (double d : data_band_abs_quantile_75) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACVar:";
			for (double d : data_band_var) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACAbsCV:";
			for (double d : data_band_abs_cv) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACIQR:";
			for (double d : data_band_IQR) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACRange:";
			for (double d : data_band_range) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACPitch:";
			for (double d : data_band_pitch) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACDomFreqRatio:";
			for (double d : data_band_dom_freq_ratio) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACMCR:";
			for (double d : data_band_MCR) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACEnergy:";
			for (double d : data_energy) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACBandEnergy:";
			for (double d : data_band_energy) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACLowEnergy:";
			for (double d : data_low_energy) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACModVigEnergy:";
			for (double d : data_modvig_energy) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n DCTotalMean:";
			for (double d : data_low_total_mean) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n DCPostureDist:";
			for (int i = 0; i < data_low_posture_dist.length; i++) {
				for (int j = 0; j < data_low_posture_dist[i].length; j++) {
					ret += String.format("%.2f", data_low_posture_dist[i][j])
							.toString() + ",";
				}
			}
			ret += "\n ACTotalAbsArea:";
			for (double d : data_band_total_abs_area) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			ret += "\n ACTotalSVM:";
			for (double d : data_band_total_SVM) {
				ret += String.format("%.2f", d).toString() + ",";
			}
			return ret;
		}
	};
}
