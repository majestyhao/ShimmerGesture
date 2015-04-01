package edu.ucdavis.myshimmerapp.ml;

import java.util.Arrays;
import java.util.List;

public final class MyAttributes {

	private final static String[] attr_names_i = { "DCMean_1", "DCArea_1",
			"ACAbsMean_1", "ACAbsArea_1", "ACEntropy_1", "ACSkew_1", "ACKur_1",
			"ACQuartiles1_1", "ACQuartiles2_1", "ACQuartiles3_1", "ACVar_1",
			"ACAbsCV_1", "ACIQR_1", "ACRange_1", "ACEnergy_1",
			"ACBandEnergy_1", "ACLowEnergy_1", "ACModVigEnergy_1", "ACPitch_1",
			"ACDomFreqRatio_1", "ACMCR_1",

			"DCMean_2", "DCArea_2", "ACAbsMean_2", "ACAbsArea_2",
			"ACEntropy_2", "ACSkew_2", "ACKur_2", "ACQuartiles1_2",
			"ACQuartiles2_2", "ACQuartiles3_2", "ACVar_2", "ACAbsCV_2",
			"ACIQR_2", "ACRange_2", "ACEnergy_2", "ACBandEnergy_2",
			"ACLowEnergy_2", "ACModVigEnergy_2", "ACPitch_2",
			"ACDomFreqRatio_2", "ACMCR_2",

			"DCMean_3", "DCArea_3", "ACAbsMean_3", "ACAbsArea_3",
			"ACEntropy_3", "ACSkew_3", "ACKur_3", "ACQuartiles1_3",
			"ACQuartiles2_3", "ACQuartiles3_3", "ACVar_3", "ACAbsCV_3",
			"ACIQR_3", "ACRange_3", "ACEnergy_3", "ACBandEnergy_3",
			"ACLowEnergy_3", "ACModVigEnergy_3", "ACPitch_3",
			"ACDomFreqRatio_3", "ACMCR_3",

			"DCMean_4", "DCArea_4", "ACAbsMean_4", "ACAbsArea_4",
			"ACEntropy_4", "ACSkew_4", "ACKur_4", "ACQuartiles1_4",
			"ACQuartiles2_4", "ACQuartiles3_4", "ACVar_4", "ACAbsCV_4",
			"ACIQR_4", "ACRange_4", "ACEnergy_4", "ACBandEnergy_4",
			"ACLowEnergy_4", "ACModVigEnergy_4", "ACPitch_4",
			"ACDomFreqRatio_4", "ACMCR_4",

			"DCMean_5", "DCArea_5", "ACAbsMean_5", "ACAbsArea_5",
			"ACEntropy_5", "ACSkew_5", "ACKur_5", "ACQuartiles1_5",
			"ACQuartiles2_5", "ACQuartiles3_5", "ACVar_5", "ACAbsCV_5",
			"ACIQR_5", "ACRange_5", "ACEnergy_5", "ACBandEnergy_5",
			"ACLowEnergy_5", "ACModVigEnergy_5", "ACPitch_5",
			"ACDomFreqRatio_5", "ACMCR_5",

			"DCMean_6", "DCArea_6", "ACAbsMean_6", "ACAbsArea_6",
			"ACEntropy_6", "ACSkew_6", "ACKur_6", "ACQuartiles1_6",
			"ACQuartiles2_6", "ACQuartiles3_6", "ACVar_6", "ACAbsCV_6",
			"ACIQR_6", "ACRange_6", "ACEnergy_6", "ACBandEnergy_6",
			"ACLowEnergy_6", "ACModVigEnergy_6", "ACPitch_6",
			"ACDomFreqRatio_6", "ACMCR_6", };

	private final static String[] attr_names_t = { "DCTotalMean_1",
			"DCPostureDist1_1", "DCPostureDist2_1", "DCPostureDist3_1",
			"ACTotalAbsArea_1", "ACTotalSVM_1",

			"DCTotalMean_2", "DCPostureDist1_2", "DCPostureDist2_2",
			"DCPostureDist3_2", "ACTotalAbsArea_2", "ACTotalSVM_2" };
	

	public final static List<String> attr_list_i = Arrays
			.asList(MyAttributes.attr_names_i);

	public final static List<String> attr_list_t = Arrays
			.asList(MyAttributes.attr_names_t);

}
