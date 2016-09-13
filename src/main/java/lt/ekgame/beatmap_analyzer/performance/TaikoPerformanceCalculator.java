package lt.ekgame.beatmap_analyzer.performance;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class TaikoPerformanceCalculator extends PerformanceCalculator {
	
	private TaikoBeatmap beatmap;
	
	public TaikoPerformanceCalculator(TaikoBeatmap beatmap) {
		this.beatmap = beatmap;
	}

	@Override
	public Performance calculate(int combo, int num100, int num50, int misses, ScoreVersion version) {
		double multiplier = 1.1;
		
		if (beatmap.getMods().has(Mod.NO_FAIL))
			multiplier *= 0.9;
		
		if (beatmap.getMods().has(Mod.SPUN_OUT))
			multiplier *= 0.95;
		
		if (beatmap.getMods().has(Mod.HIDDEN))
			multiplier *= 1.1;
		
		double accuracy = calculateAccuracyCombo(beatmap.getMaxCombo(), num100, num50, misses);
		double strainValue = calculateStrainValue(combo, num100, num50, misses);
		double accValue = calculateAccuracyValue(combo, num100, num50, misses);
		double performance = Math.pow(Math.pow(strainValue, 1.1) + Math.pow(accValue, 1.1), 1/1.1)*multiplier;
		
		return new Performance(accuracy, performance, 0, strainValue, accValue);
	}
	
	private double calculateStrainValue(int combo, int num100, int num50, int misses) {
		Difficulty diff = beatmap.getDifficulty();
		double strainValue = Math.pow(5*Math.max(1, diff.getStarDifficulty()/0.0075) - 4, 2)/100000;
		double lengthBonus = 1 + 0.1 * Math.min(1, getObjectCount() / 1500.0);
		strainValue *= lengthBonus;
		
		// miss penalty
		strainValue *= Math.pow(0.985, misses);
		
		int maxCombo = beatmap.getMaxCombo();
		if (maxCombo > 0)
			strainValue *= Math.min(Math.pow(combo, 0.5)/Math.pow(maxCombo, 0.5), 1);
		
		if (beatmap.getMods().has(Mod.HIDDEN))
			strainValue *= 1.025;
		
		if (beatmap.getMods().has(Mod.FLASHLIGHT))
			strainValue *= 1.05 * lengthBonus;
		
		return strainValue*calculateAccuracyCombo(maxCombo, num100, num50, misses);
	}
	
	private double calculateAccuracyValue(int combo, int num100, int num50, int misses) {
		int perfectHitWindow = (int) (MathUtils.getHitWindow300(beatmap.getDifficultySettings().getOD(), Gamemode.TAIKO, beatmap.getMods())/beatmap.getMods().getTimeRate());
		if (perfectHitWindow <= 0)
			return 0;
		
		double accuracy = calculateAccuracyCombo(beatmap.getMaxCombo(), num100, num50, misses);
		double accValue = Math.pow(150.0/perfectHitWindow, 1.1) * Math.pow(accuracy, 15)*22;
		return accValue*Math.min(1.15, Math.pow(getObjectCount()/1500.0, 0.3));
	}
	
	@Override
	protected double calculateAccuracy(int num300, int num100, int num50, int numMiss) {
		int total = num300 + num100 + num50 + numMiss;
		if (total > 0)
			return MathUtils.clamp(0, 1, (num300*300 + num100*150)/((double)total*300));
		return 0;
	}

	@Override
	public int getObjectCount() {
		return beatmap.getMaxCombo();
	}

}
