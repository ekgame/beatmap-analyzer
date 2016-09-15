package lt.ekgame.beatmap_analyzer.utils;

import lt.ekgame.beatmap_analyzer.Gamemode;

public class MathUtils {
	
	private static final double 
		ODMinMs = 80, // OD 0
		ODMaxMs = 20, // OD 10
		ARMinMs = 1800, // AR 0
		ARMidMs = 1200, // AR 5
		ARMaxMs = 450;  // AR 10
	
	private static final double 
		ODStep = 6,
		ARStepLow = 120,  // AR 0-5
		ARStepHigh = 150; // AR 5-10
	
	public static double clamp(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}
	
	public static double difficultyRange(double diff, double min, double mid, double max, Mods mods) {
		if (mods.has(Mod.EASY))
			diff = Math.max(0, diff/2);
		
		if (mods.has(Mod.HARDROCK))
			diff = Math.min(10, diff*1.4);
		
        return (diff > 5) ? (mid + (max - mid) * (diff - 5) / 5) 
	    	 : (diff < 5) ? (mid - (mid - min) * (5 - diff) / 5) 
	    	 : mid;
	}
	
	public static int getHitWindow50(double od, Gamemode gamemode, Mods mods) {
		switch (gamemode) {
		case OSU: return (int) difficultyRange(od, 200, 150, 100, mods);
		case TAIKO: return (int) difficultyRange(od, 135, 95, 70, mods);
		case CATCH: return -1;
		case MANIA: return -1; // TODO: mania timing
		default: return -1;
		}
	}
	
	public static int getHitWindow100(double od, Gamemode gamemode, Mods mods) {
        switch (gamemode) {
		case OSU: return (int) difficultyRange(od, 140, 100, 60, mods);
		case TAIKO: return (int) difficultyRange(od, 120, 80, 50, mods);
		case CATCH: return -1;
		case MANIA: return -1;  // TODO: mania timing
		default: return -1;
		}
	}
	
	public static int getHitWindow300(double od, Gamemode gamemode, Mods mods) {
		switch (gamemode) {
		case OSU: return (int) difficultyRange(od, 80, 50, 20, mods);
		case TAIKO: return (int) difficultyRange(od, 50, 35, 20, mods);
		case CATCH: return -1;
		case MANIA: return -1;  // TODO: mania timing
		default: return -1;
		}
	}
	
	public static double recalculateOverallDifficulty(double od, double multiplier, double speedMultiplier) {
		double overallDifficulty = od*multiplier;
		double overallDifficultyTime = ODMinMs - Math.ceil(ODStep*overallDifficulty);
		overallDifficultyTime = MathUtils.clamp(ODMaxMs, ODMinMs, overallDifficultyTime/speedMultiplier);
		return (ODMinMs - overallDifficultyTime)/ODStep;
	}
	
	public static double recalculateApproachRate(double ar, double multiplier, double speedMultiplier) {
		double approachRate = ar*multiplier;
		double approachRateTime = approachRate <= 5 ? (ARMinMs - ARStepLow*approachRate) : (ARMidMs - ARStepHigh*(approachRate - 5));
		approachRateTime = MathUtils.clamp(ARMaxMs, ARMinMs, approachRateTime/speedMultiplier);
		return approachRate <= 5 ? ((ARMinMs - approachRateTime)/ARStepLow) : (5 + (ARMidMs - approachRateTime)/ARStepHigh);
	}
	
	public static double recalculateCircleSize(double cs, double multiplier) {
		double circleSize = cs*multiplier;
		return MathUtils.clamp(0, 10, circleSize);
	}
	
	public static int calculateManiaCollumn(double x, int collumns) {
		return (int) (x/(512.0/collumns));
	}
	
	public static double calculateOsuAccuracy(int num300, int num100, int num50, int numMiss) {
		int total = num300 + num100 + num50 + numMiss;
		return total == 0 ? 0 : ((num300*6 + num100*2 + num50)/(total*6.0));
	}
	
	public static double calculateTaikoAccuracy(int numGreat, int numHalf, int numMiss) {
		int total = numGreat + numHalf + numMiss;
		return total == 0 ? 0 : ((numGreat*2 + numHalf)/(total*2.0));
	}
}