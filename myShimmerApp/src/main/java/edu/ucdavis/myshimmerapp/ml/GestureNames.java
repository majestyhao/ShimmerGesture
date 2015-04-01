package edu.ucdavis.myshimmerapp.ml;

public class GestureNames {

	private final static String[] fingers = { "f_One", "f_Two", "f_Three",
			"f_Four", "f_Five", "f_One Twice", "f_Two Twice", "f_Single Click",
			"f_Double Click", "f_Zoom In", "f_Zoom Out", "f_Thumb Up" };

	private final static String[] hands = { "h_Clockwise Circle",
			"h_Anti-Clockwise Circle", "h_Down Once", "h_Down Twice",
			"h_Up Once", "h_Up Twice", "h_Left Once", "h_Left Twice",
			"h_Right Once", "h_Right Twice", "h_Rotate Left", "h_Rotate Right",
			"h_Gun Shot", "h_Phone Call" };

	private final static String[] writings = { "w_A", "w_B", "w_C", "w_D",
			"w_E", "w_F", "w_G", "w_H" };

	public final static String[][] types = { fingers, hands, writings };

}
