package edu.ucdavis.myshimmerapp.ml;

public class GestureTypes {

	public final static String[] fingers = { "f_One", "f_Two", "f_Three",
			"f_Four", "f_Five", "f_One Twice", "f_Two Twice", "f_Thumb Up",
			"f_Single Click", "f_Double Click" };

	public final static String[] hands = { "h_Clockwise Circle",
			"h_Anti-Clockwise Circle", "h_Down Once", "h_Down Twice",
			"h_Up Once", "h_Up Twice", "h_Gun Shot", "h_Left Once",
			"h_Left Twice", "h_Right Once", "h_Right Twice", "h_Phone Call",
			"h_Rotate Left", "h_Rotate Right", "h_Volumn Up", "h_Volumn Down" };

	public final static String[] arms = { "a_Thumb Down", "a_Push",
			"a_Left Once", "a_Left Twice", "a_Right Once", "a_Right Twice",
			"a_Clockwise Circle", "a_Anti-Clockwise Circle", "a_Up" };

	public final static String[][] types = { fingers, hands, arms };

}
