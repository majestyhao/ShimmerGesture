package edu.ucdavis.myshimmerapp.ml;

import android.util.Log;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

import java.util.List;

import biz.source_code.dsp.filter.FilterPassType;
import biz.source_code.dsp.filter.IirFilterCoefficients;
import edu.ucdavis.myshimmerapp.activities.MyShimmerDataList;

public class FeatureConstructor {

	private final static String TAG = "MyShimmerApp.FeatureConstructor";
	private final static String classPrefix = "edu.ucdavis.myshimmerapp.ml.FeatureConstructor$";

	private static double[][] datas;
	private static double[][] lowpass;
	private static double[][] bandpass;
	private static double[][] bandpassAbs;
	private static double[][] bandenergy;
	private static double[][] lowenergy;
	private static double[][] modvigenergy;

	private static DescriptiveStatistics[] stat_data_band = new DescriptiveStatistics[6];
	private static DescriptiveStatistics[] stat_data_band_abs = new DescriptiveStatistics[6];

	private static FastFourierTransformer fft = new FastFourierTransformer(
			DftNormalization.STANDARD);

	// private static Complex[][] data_band_fft = new Complex[6][];

	private IirFilterCoefficients lowPass_coef;
	private IirFilterCoefficients bandPass_coef;
	private IirFilterCoefficients bandEnergy_coef;
	private IirFilterCoefficients lowEnergy_coef;
	private IirFilterCoefficients modvigEnergy_coef;

	private final double lowPass_Fcf1 = 6.4;
	private final double bandPass_Fcf1 = 0.64;
	private final double bandPass_Fcf2 = 57.6;
	private final double bandEnergy_Fcf1 = 1.92;
	private final double bandEnergy_Fcf2 = 22.4;
	private final double lowEnergy_Fcf1 = 4.48;
	private final double modvigEnergy_Fcf1 = 4.52;
	private final double modvigEnergy_Fcf2 = 57.6;

	private double samplingRate = 0;
	private final int filterOrder = 2;

	private Features feature;

	public FeatureConstructor(MyShimmerDataList input, double sampleRt,
			List<String> attrNames) {
		if (input != null && !input.isEmpty()) {
			int dataSize = input.size();
			Log.d(TAG, "construct features:" + dataSize + ",samplingRate:"
					+ sampleRt);

			samplingRate = sampleRt;

			/* get filter coefficients */
			lowPass_coef = Utils.getFilterCoef(FilterPassType.lowpass,
					filterOrder, lowPass_Fcf1 / samplingRate, 0);
			bandPass_coef = Utils.getFilterCoef(FilterPassType.bandpass,
					filterOrder, bandPass_Fcf1 / samplingRate, bandPass_Fcf2
							/ samplingRate);
			bandEnergy_coef = Utils.getFilterCoef(FilterPassType.bandpass,
					filterOrder, bandEnergy_Fcf1 / samplingRate,
					bandEnergy_Fcf2 / samplingRate);
			lowEnergy_coef = Utils.getFilterCoef(FilterPassType.lowpass,
					filterOrder, lowEnergy_Fcf1 / samplingRate, 0);
			modvigEnergy_coef = Utils.getFilterCoef(FilterPassType.bandpass,
					filterOrder, modvigEnergy_Fcf1 / samplingRate,
					modvigEnergy_Fcf2 / samplingRate);

			/* initialize buffers */
			datas = new double[6][dataSize];

			lowpass = new double[6][dataSize];
			bandpass = new double[6][dataSize];
			bandpassAbs = new double[6][dataSize];
			bandenergy = new double[6][dataSize];
			lowenergy = new double[6][dataSize];
			modvigenergy = new double[6][dataSize];

			for (int i = 0; i < 6; i++) {
				datas[i] = input.getSerie(i);
			}
			/* get filtered data, and statistic descriptives */
			for (int i = 0; i < 6; i++) {

				// lowpass[i] = datas[i];
				// bandpass[i] = datas[i];
				// bandenergy[i] = datas[i];
				// lowenergy[i] = datas[i];
				// modvigenergy[i] = datas[i];

				// Utils.logTimeDom(datas[i], "datas_" + i);
				lowpass[i] = Utils.filtfilt(lowPass_coef, datas[i]);
				// Utils.logTimeDom(lowpass[i], "lowpass_" + i);
				bandpass[i] = Utils.filtfilt(bandPass_coef, datas[i]);
				// Utils.logTimeDom(lowpass[i], "bandpass" + i);

				bandenergy[i] = Utils.filtfilt(bandEnergy_coef, datas[i]);
				lowenergy[i] = Utils.filtfilt(lowEnergy_coef, datas[i]);
				modvigenergy[i] = Utils.filtfilt(modvigEnergy_coef, datas[i]);

				for (int j = 0; j < bandpass[i].length; j++) {
					bandpassAbs[i][j] = FastMath.abs(bandpass[i][j]);
				}

				stat_data_band[i] = new DescriptiveStatistics(bandpass[i]);
				stat_data_band_abs[i] = new DescriptiveStatistics(
						bandpassAbs[i]);
			}

			feature = new Features(attrNames);

			calculateFeature();
		}
	}

	public Features getFeatures() {
		return feature;
	}

	private void calculateFeature() {
		// Log.d(TAG, "calculateFeature ");
		for (int i = 0; i < feature.size(); i++) {

			String featueName = feature.getName(i);
			String className = featueName.substring(0, featueName.length() - 2);
			int index = Integer.parseInt(featueName.substring(featueName
					.length() - 1));

			// Log.d(TAG, "className:" + className + "_" + index);
			try {

				FeatureCalculationInterface handler = (FeatureCalculationInterface) Class
						.forName(classPrefix + className).newInstance();

				feature.setValue(i, handler.calc(index - 1));
				// Log.d(TAG, String.valueOf(feature.getValue(i)));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private interface FeatureCalculationInterface {
		public double calc(int index);

	}

	public static class DCMean implements FeatureCalculationInterface {
		@Override
		public double calc(int index) {
			return StatUtils.mean(lowpass[index]);
		}

		public DCMean() {

		}
	}

	public static class DCArea implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return StatUtils.sum(lowpass[i]);
		}

		public DCArea() {

		}
	}

	public static class ACAbsMean implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getMean();
		}

		public ACAbsMean() {

		}
	}

	public static class ACAbsArea implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getSum();
		}

		public ACAbsArea() {

		}
	}

	public static class ACEntropy implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			double ret = 0;
			Complex[] input = Utils.doFFT(fft, bandpass[i]);

			if (input != null && input.length != 0) {
				int length = input.length;
				double[] abs = new double[length];
				double sum = 0;
				double[] norm = new double[length];
				double[] norm_log2s = new double[length];
				for (int j = 0; j < length; j++) {
					abs[j] = input[j].abs();
				}
				sum = StatUtils.sum(abs);
				for (int j = 0; j < length; j++) {
					norm[j] = abs[j] / sum;
					norm_log2s[j] = FastMath.log(2, norm[j]);
					ret += (norm[j] * norm_log2s[j]);
				}
				ret = ret / length;
			}
			return ret;
		}

		public ACEntropy() {

		}
	}

	public static class ACSkew implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band[i].getSkewness();
		}

		public ACSkew() {

		}
	}

	public static class ACKur implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band[i].getKurtosis();
		}

		public ACKur() {

		}
	}

	public static class ACQuartiles1 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getPercentile(25);
		}

		public ACQuartiles1() {

		}
	}

	public static class ACQuartiles2 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getPercentile(50);
		}

		public ACQuartiles2() {

		}
	}

	public static class ACQuartiles3 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getPercentile(75);
		}

		public ACQuartiles3() {

		}
	}

	public static class ACVar implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band[i].getVariance();
		}

		public ACVar() {

		}
	}

	public static class ACAbsCV implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getStandardDeviation()
					/ stat_data_band_abs[i].getMean() * 100;
		}

		public ACAbsCV() {

		}
	}

	public static class ACIQR implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i].getPercentile(75)
					/ stat_data_band_abs[i].getPercentile(25);
		}

		public ACIQR() {

		}
	}

	public static class ACRange implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band[i].getMax() - stat_data_band[i].getMin();
		}

		public ACRange() {

		}
	}

	public static class ACEnergy implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return Utils.getEnergy(fft, datas[i]);
		}

		public ACEnergy() {

		}
	}

	public static class ACBandEnergy implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return Utils.getEnergy(fft, bandenergy[i]);
		}

		public ACBandEnergy() {

		}
	}

	public static class ACLowEnergy implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return Utils.getEnergy(fft, lowenergy[i]);
		}

		public ACLowEnergy() {

		}
	}

	public static class ACModVigEnergy implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return Utils.getEnergy(fft, modvigenergy[i]);
		}

		public ACModVigEnergy() {

		}
	}

	public static class ACPitch implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return 0;// TODO
		}

		public ACPitch() {

		}
	}

	public static class ACDomFreqRatio implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			double ret = 0;
			Complex[] input = Utils.doFFT(fft, bandpass[i]);

			if (input != null && input.length != 0) {
				int length = input.length / 2 + 1;
				double[] abs = new double[length];
				double sum = 0;
				for (int j = 0; j < length; j++) {
					abs[j] = input[j].abs();
				}
				MathArrays.sortInPlace(abs, OrderDirection.DECREASING);
				sum = StatUtils.sum(abs, 0, length / 2 + 1);
				ret = abs[0] / sum;
			}
			return ret;
		}

		public ACDomFreqRatio() {

		}
	}

	public static class ACMCR implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			double ret = 0;
			double[] input = bandpass[i];
			double mean = stat_data_band[i].getMean();

			if (input != null && input.length != 0) {
				for (int j = 0; j < input.length - 1; j++) {
					if (((input[j] - mean) > 0) != ((input[j + 1] - mean) > 0)) {
						ret++;
					}
				}
				ret = ret / (input.length - 1);
			}
			return ret;
		}

		public ACMCR() {

		}
	}

	public static class DCTotalMean implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {

			return (StatUtils.mean(lowpass[i * 3])
					+ StatUtils.mean(lowpass[i * 3 + 1]) + StatUtils
						.mean(lowpass[i * 3 + 2]));
		}

		public DCTotalMean() {

		}
	}

	public static class DCPostureDist1 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return StatUtils.mean(lowpass[i * 3])
					- StatUtils.mean(lowpass[i * 3 + 1]);
		}

		public DCPostureDist1() {

		}
	}

	public static class DCPostureDist2 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return StatUtils.mean(lowpass[i * 3 + 1])
					- StatUtils.mean(lowpass[i * 3 + 2]);
		}

		public DCPostureDist2() {

		}
	}

	public static class DCPostureDist3 implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return StatUtils.mean(lowpass[i * 3 + 2])
					- StatUtils.mean(lowpass[i * 3]);
		}

		public DCPostureDist3() {

		}
	}

	public static class ACTotalAbsArea implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			return stat_data_band_abs[i * 3].getSum()
					+ stat_data_band_abs[i * 3 + 1].getSum()
					+ stat_data_band_abs[i * 3 + 2].getSum();
		}

		public ACTotalAbsArea() {

		}
	}

	public static class ACTotalSVM implements FeatureCalculationInterface {
		@Override
		public double calc(int i) {
			double ret = 0;
			double[] x = bandpass[i * 3];
			double[] y = bandpass[i * 3 + 1];
			double[] z = bandpass[i * 3 + 2];

			if (x != null && y != null && z != null) {
				double[] sqrts = new double[x.length];
				for (int j = 0; j < x.length; j++) {
					sqrts[j] = Utils.fastSqrt(FastMath.pow(x[j], 2),
							FastMath.pow(y[j], 2), FastMath.pow(z[j], 2));
				}
				ret = StatUtils.mean(sqrts);
			}
			return ret;
		}

		public ACTotalSVM() {

		}
	}
}
