package edu.ucdavis.myshimmerapp.ml;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.math3.util.MathArrays;
import org.apache.commons.math3.util.MathArrays.OrderDirection;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.myshimmerapp.activities.MainActivity;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Model {
	private final static String TAG = "MyShimmerApp.Model";

	protected Instances trainingDataSet;
	protected SimpleLogistic logistic;
	protected RandomForest forest;

	protected Classifier classifier;
	protected static Instances modelDataSet;
	private boolean isInitializedforValication = false;

	private int ml_algorithm = MainActivity.ML_ALGORITHM_SIMPLE_LOGISTIC;
	private int gesture_type = MainActivity.GESTURE_TYPE_FINGER;

	String[] classifierFileNames_l = { "finger__l.model", "hand__l.model",
			"writings__l.model" };
	String[] classifierFileNames_t = { "finger__t.model", "hand__t.model",
			"writings__t.model" };
	String[] datasetFileNames = { "finger.data", "hand.data", "writings.data" };
	String logFilePath = Environment.getExternalStorageDirectory()
			+ "/ShimmerTest/Models/";

	public Model(int mlType, int gestureType, boolean isTraining) {
		if (mlType >= MainActivity.ML_ALGORITHM_SIMPLE_LOGISTIC
				&& mlType <= MainActivity.ML_ALGORITHM_DECISION_TREE) {
			ml_algorithm = mlType;
		}

		if (gestureType >= MainActivity.GESTURE_TYPE_FINGER
				&& gestureType <= MainActivity.GESTURE_TYPE_ARM) {
			gesture_type = gestureType;
		}

		if (isTraining) {
			FastVector trainingAttrVector = new FastVector();

			/* construct feature attributes */
			for (int i = 0; i < MyAttributes.attr_list_i.size(); i++) {
				trainingAttrVector.addElement(new Attribute(
						MyAttributes.attr_list_i.get(i)));
			}
			for (int i = 0; i < MyAttributes.attr_list_t.size(); i++) {
				trainingAttrVector.addElement(new Attribute(
						MyAttributes.attr_list_t.get(i)));
			}

			FastVector fvClassVal = new FastVector(
					GestureNames.types[gesture_type].length);

			/* add class attribute */
			fvClassVal = new FastVector(GestureNames.types[gesture_type].length);
			for (String str : GestureNames.types[gesture_type]) {
				fvClassVal.addElement(str);
			}
			Attribute ClassAttribute = new Attribute(new String("Class"),
					fvClassVal);
			trainingAttrVector.addElement(ClassAttribute);

			/* construct training data set format */
			trainingDataSet = new Instances("Training", trainingAttrVector, 0);
			trainingDataSet.setClassIndex(trainingAttrVector.size() - 1);

			logistic = new SimpleLogistic();
			forest = new RandomForest();

			Log.d(TAG,
					"Training Model Initialized:"
							+ trainingDataSet.numAttributes());
		} else {

			loadModelClassifier();
		}
	}

	public void addInstanceForTraining(Features list, String instLabel) {
		if (list != null && instLabel != null) {

			int attr_count = trainingDataSet.numAttributes();
			Instance instance = new Instance(attr_count);

			/* fill in feature attribute values */
			Log.d(TAG,
					"addInstance:" + list.size() + ","
							+ trainingDataSet.numAttributes() + "," + instLabel);
			for (int i = 0; i < attr_count - 1; i++) {
				// Log.d(TAG,
				// "i:" + i + "," + list.getName(i) + ","
				// + list.getValue(i));
				instance.setValue(trainingDataSet.attribute(list.getName(i)),
						list.getValue(i));
			}
			instance.setDataset(trainingDataSet);

			/* fill in class value */
			instance.setValue(trainingDataSet.attribute(attr_count - 1),
					instLabel);

			/* add to training data set */
			trainingDataSet.add(instance);
			Log.d(TAG, "instance.classIndex():" + instance.classIndex() + ","
					+ instance.classAttribute().type());
		}
	}

	public boolean buildClassfiers() {
		boolean ret = false;
		Log.d(TAG, "build");

		if (logWholeTrainingDataSet()) {
			InfoGainAttributeEval infoGainEval = new InfoGainAttributeEval();
			try {
				/* Attribute Selection */
				{
					int origAttrCount = trainingDataSet.numAttributes() - 1;/*
																			 * except
																			 * class
																			 * attributes
																			 */

					double[] scores = new double[origAttrCount];
					double[] indexs = new double[origAttrCount];

					infoGainEval.buildEvaluator(trainingDataSet);
					Log.d(TAG, "attribute selection" + infoGainEval.toString());

					Log.d(TAG, "original dataSet.numAttributes():"
							+ trainingDataSet.numAttributes());
					Log.d(TAG, "original dataSet.classIndex():"
							+ trainingDataSet.classIndex());

					for (int i = 0; i < origAttrCount; i++) {
						indexs[i] = i;
						scores[i] = infoGainEval.evaluateAttribute(i);
					}

					for (int i = 0; i < origAttrCount; i++) {
						Log.d(TAG, "scores, "
								+ trainingDataSet.attribute(i).name() + ":"
								+ scores[i]);
					}

					MathArrays.sortInPlace(scores, OrderDirection.DECREASING,
							indexs);

					int startPoint = 0;
					for (int i = 0; i < origAttrCount; i++) {
						if (scores[i] <= 1 || i >= 20) {
							startPoint = i;
							break;
						}
					}
					int[] indicesToRemove = new int[origAttrCount - startPoint];

					for (int i = 0; i < indicesToRemove.length; i++) {
						indicesToRemove[i] = (int) indexs[i + startPoint];
					}

					/* use filter to remove */
					Remove remove = new Remove();
					remove.setAttributeIndicesArray(indicesToRemove);
					remove.setInputFormat(trainingDataSet);

					modelDataSet = Filter.useFilter(trainingDataSet, remove);
					Log.d(TAG,
							"afterwards newdataSet.numAttributes():"
									+ modelDataSet.numAttributes()
									+ ",newdataSet.numInstances():"
									+ modelDataSet.numInstances());
					for (int i = 0; i < modelDataSet.numAttributes(); i++) {
						Log.d(TAG, modelDataSet.attribute(i).name());
					}
				}

				/* Build Classifier */
				logistic.buildClassifier(modelDataSet);

				Log.d(TAG, "SimpleLogistic:" + logistic.toString());
				forest.buildClassifier(modelDataSet);
				Log.d(TAG, "RandomForest Options:" + forest.getOptions());
				Log.d(TAG, "RandomForest:" + forest.toString());

				saveModelClassifier();

				/* print evaluations */
				Evaluation eval = new Evaluation(modelDataSet);
				eval.evaluateModel(logistic, modelDataSet);
				Log.d(TAG, "eval logistic:" + eval.toSummaryString());
				eval.evaluateModel(forest, modelDataSet);
				Log.d(TAG, "eval forest:" + eval.toSummaryString());

				ret = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	public int classify(Features list) {
		double ret = Double.NaN;
		int attr_count = modelDataSet.numAttributes();
		Log.d(TAG, "Classify");
		if (list != null && list.size() == attr_count - 1) {
			Instance instance = new Instance(attr_count);

			Log.d(TAG, "addInstance:" + list.size() + "," + attr_count);
			for (int i = 0; i < attr_count - 1; i++) {
				// Log.d(TAG, i + ":" + list.getName(i) + "," +
				// list.getValue(i));
				instance.setValue(modelDataSet.attribute(list.getName(i)),
						list.getValue(i));
			}
			instance.setDataset(modelDataSet);

			try {
				ret = classifier.classifyInstance(instance);
				Log.d(TAG, "classify result:" + ret + ","
						+ instance.classAttribute().value((int) ret));

				double[] distribution;
				if (ml_algorithm == MainActivity.ML_ALGORITHM_SIMPLE_LOGISTIC) {
					distribution = ((SimpleLogistic) classifier)
							.distributionForInstance(instance);
				} else {
					distribution = ((RandomForest) classifier)
							.distributionForInstance(instance);
				}
				for (int i = 0; i < distribution.length; i++) {
					Log.d(TAG, "classify result-distribution" + "["
							+ GestureNames.types[gesture_type][i] + "]"
							+ distribution[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return (int) ret;
	}

	private void saveModelClassifier() {
		if (modelDataSet != null && logistic != null && forest != null) {
			try {
				/* save model data */
				if (SerializationHelper.isSerializable(modelDataSet.getClass())) {
					File outFile = new File(logFilePath,
							datasetFileNames[gesture_type]);
					SerializationHelper.write(new FileOutputStream(outFile),
							modelDataSet);
				}

				/* save classifier */
				if (SerializationHelper.isSerializable(logistic.getClass())) {
					File outFile = new File(logFilePath,
							classifierFileNames_l[gesture_type]);
					SerializationHelper.write(new FileOutputStream(outFile),
							logistic);
				}

				if (SerializationHelper.isSerializable(forest.getClass())) {
					File outFile = new File(logFilePath,
							classifierFileNames_t[gesture_type]);
					SerializationHelper.write(new FileOutputStream(outFile),
							forest);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean loadModelClassifier() {
		boolean ret = false;
		Log.d(TAG, "loadModelClassifier");
		try {
			/* load classifier */
			File inFile_classifier = null;
			if (ml_algorithm == MainActivity.ML_ALGORITHM_SIMPLE_LOGISTIC) {
				inFile_classifier = new File(logFilePath,
						classifierFileNames_l[gesture_type]);
			} else {
				inFile_classifier = new File(logFilePath,
						classifierFileNames_t[gesture_type]);
			}
			Log.d(TAG, "inFile_classifier:" + inFile_classifier.getName());
			classifier = (Classifier) SerializationHelper
					.read(new FileInputStream(inFile_classifier));

			/* load model data */
			File inFile_2 = new File(logFilePath,
					datasetFileNames[gesture_type]);
			modelDataSet = (Instances) SerializationHelper
					.read(new FileInputStream(inFile_2));
			Log.d(TAG, "inFile_2:" + inFile_2.getName());

			Log.d(TAG, "Validation Model Initialized");
			Log.d(TAG, "classifier:" + classifier.toString());
			Log.d(TAG, "modelDataSet:" + modelDataSet.numAttributes() + ","
					+ modelDataSet.numInstances());

			// for (int i = 0; i < modelDataSet.numAttributes(); i++) {
			// Log.d(TAG, modelDataSet.attribute(i).name());
			// }

			isInitializedforValication = true;
			ret = true;
		} catch (Exception e) {

			e.printStackTrace();
		}
		return ret;
	}

	public boolean isInitializedforValidation() {
		return isInitializedforValication;
	}

	public List<String> getLoadedModelAttrNames() {
		if (isInitializedforValication) {
			List<String> names = new ArrayList<String>();
			if (modelDataSet != null) {
				for (int i = 0; i < modelDataSet.numAttributes(); i++) {
					names.add(modelDataSet.attribute(i).name());
				}
			}
			return names;
		} else {
			return null;
		}
	}

	private boolean logWholeTrainingDataSet() {
		if (trainingDataSet != null) {
			String logFileAppendix = ".txt";
			String logFilePath = Environment.getExternalStorageDirectory()
					+ "/ShimmerTest/Logs";
			String wholeLogFileName;
			File wholeLogFile;
			BufferedWriter wholeBuf;

			wholeLogFileName = "Training_DataSet" + logFileAppendix;
			wholeLogFile = new File(logFilePath, wholeLogFileName);

			if (!wholeLogFile.exists()) {
				try {
					wholeLogFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				wholeBuf = new BufferedWriter(
						new FileWriter(wholeLogFile, true));

				for (int i = 0; i < trainingDataSet.numInstances(); i++) {
					double[] attrs = trainingDataSet.instance(i)
							.toDoubleArray();
					int numAttr = attrs.length;
					String str = "";
					for (int j = 0; j < numAttr; j++) {
						str += String.valueOf(attrs[j]) + " , ";
					}

					wholeBuf.append(str.substring(0, str.length() - 5));
					wholeBuf.newLine();
				}

				wholeBuf.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
		return false;
	}
}
