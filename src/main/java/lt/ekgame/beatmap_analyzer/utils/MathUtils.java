package lt.ekgame.beatmap_analyzer.utils;

public class MathUtils {
	
	public static double clamp(double min, double max, double value) {
		return Math.min(max, Math.max(min, value));
	}

}
