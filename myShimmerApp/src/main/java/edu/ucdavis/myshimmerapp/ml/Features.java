package edu.ucdavis.myshimmerapp.ml;

import java.util.ArrayList;
import java.util.List;

public class Features {

	private final static String TAG = "MyShimmerApp.AllFeatures";

	private List<FeatureClass> list = new ArrayList<FeatureClass>();

	public Features() {
		for (String str : MyAttributes.attr_list_i) {
			list.add(new FeatureClass(str));
		}
		for (String str : MyAttributes.attr_list_t) {
			list.add(new FeatureClass(str));
		}
	}

	public Features(List<String> attrNames) {
//		Log.d(TAG, "Features()," + attrNames);
		if (attrNames == null || attrNames.isEmpty()) {
			for (String str : MyAttributes.attr_list_i) {
				list.add(new FeatureClass(str));
			}
			for (String str : MyAttributes.attr_list_t) {
				list.add(new FeatureClass(str));
			}
		} else {
			for (String str : attrNames) {
				if (MyAttributes.attr_list_i.contains(str)
						|| MyAttributes.attr_list_t.contains(str)) {
					list.add(new FeatureClass(str));
				}
			}
		}
	}

	public int size() {
		return list.size();
	}

	public String getName(int i) {
		return list.get(i).getName();
	}

	public double getValue(int i) {
		return list.get(i).getValue();
	}

	public void setValue(int i, double d) {
		list.get(i).setValue(d);
	}

	public String toString() {
		String ret = "features:";
		for (int i = 0; i < list.size(); i++) {
			FeatureClass c = list.get(i);
			ret += "\n" + c.getName() + ":" + c.getValue();
		}
		return ret;
	}

	public class FeatureClass {
		private String name;
		private double value = 0;

		public FeatureClass(String n) {
			name = n;
		}

		public String getName() {
			return name;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double d) {
			value = d;
		}
	}
}
