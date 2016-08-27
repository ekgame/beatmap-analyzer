package lt.ekgame.beatmap_analyzer.calculator;

import lt.ekgame.beatmap_analyzer.Beatmap;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapDifficulties;
import lt.ekgame.beatmap_analyzer.beatmap.osu.HitCircle;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class PerformanceCalculator {
	
	private Beatmap beatmap;

	public PerformanceCalculator(Beatmap beatmap) {
		this.beatmap = beatmap;
	}
	
	/**
	 * @param combo
	 * @param accuracy  (0-1)
	 * @param misses
	 * @param version
	 * @return
	 */
	public Performance calculate(int combo, double accuracy, int misses, ScoreVersion version) {
		misses = Math.min(beatmap.getHitObjects().size(), misses);
		int max300 = beatmap.getHitObjects().size() - misses;
		
		accuracy = Math.max(0.0, Math.min(calculateAccuracy(max300, 0, 0, misses) * 100, accuracy));
		int num50 = 0;
		int num100 = (int) Math.round(-3*((accuracy - 1)*beatmap.getHitObjects().size() + misses)*0.5);
		
		if (num100 > beatmap.getHitObjects().size() - misses) {
			num100 = 0;
			num50 = (int) Math.round(-6.0 * ((accuracy - 1) * beatmap.getHitObjects().size() + misses) * 0.2);
			num50 = Math.min(max300, num50);
		}
		else {
			num100 = Math.min(max300, num100);
		}

		//int num300 = beatmap.getHitObjects().size() - num100 - num50 - misses;
		return calculate(combo, num100, num50, misses, version);
	}
	
	public Performance calculate(int combo, int num100, int num50, int misses, ScoreVersion version) {
		int num300 = beatmap.getHitObjects().size() - num100 - num50 - misses;
		
		if (beatmap.getMaxCombo() <= 0)
			throw new IllegalStateException("Beatmap must have elements.");
		
		Difficulty difficulty = beatmap.getDifficulty();
		BeatmapDifficulties diffs = beatmap.getDifficultySettings();
		
		int total = num300 + num100 + num50 + misses;
		double accuracy = calculateAccuracy(num300, num100, num50, misses);
		
		double hitsOver2k = total/(double)2000;
		double lengthBonus = 0.95 + 0.4*Math.min(1, hitsOver2k) + (total > 2000 ? Math.log10(hitsOver2k)*0.5 : 0);
		double missPenalty = Math.pow(0.97, misses);
		double comboBreak = Math.pow(combo, 0.8)/Math.pow(beatmap.getMaxCombo(), 0.8);
		
		// aim calculation
		double ARBonus = 1;
		if (diffs.getAR() > 10.33) {
			ARBonus += 0.45*(diffs.getAR() - 10.33);
		}
		else if (diffs.getAR() < 8) {
			double lowARBonus = 0.01*(8 - diffs.getAR());
			if (beatmap.getMods().has(Mod.HIDDEN))
				lowARBonus *= 2;
			ARBonus += lowARBonus;
		}
		
		double aimValue = baseStrain(difficulty.getAimDifficulty());
		aimValue *= lengthBonus*missPenalty*comboBreak*ARBonus;
		
		if (beatmap.getMods().has(Mod.HIDDEN))
			aimValue *= 1.18;
		
		if (beatmap.getMods().has(Mod.FLASHLIGHT))
			aimValue *= 1.45*lengthBonus;
		
		double accuracyBonus = 0.5 + accuracy/2;
		double ODBonus = 0.98 + Math.pow(diffs.getOD(), 2)/2500;
		
		aimValue *= accuracyBonus*ODBonus;
		
		// speed calculation
		double speedValue = baseStrain(difficulty.getSpeedDifficulty());
		speedValue *= lengthBonus*missPenalty*comboBreak*accuracyBonus*ODBonus;
		
		// score v2
		double realAccuracy = accuracy;
		int circles = total;
		
		//score v1
		if (version == ScoreVersion.VERSION_1) {
			circles = (int) beatmap.getHitObjects().stream().filter(o->o instanceof HitCircle).count();
			if (circles > 0)
				realAccuracy = ((num300 - (total - circles))*300 + num100*100 + num50*50)/((double)circles*300);
			realAccuracy = Math.max(0, realAccuracy);
		}
		
		// accuracy calculation
		double accuracyValue = Math.pow(1.52163, diffs.getOD())*Math.pow(realAccuracy, 24.0) * 2.83;
		accuracyValue *= Math.min(1.15, Math.pow(circles/(double)1000, 0.3));
		
		if (beatmap.getMods().has(Mod.HIDDEN))
			accuracyValue *= 1.02;
		
		if (beatmap.getMods().has(Mod.FLASHLIGHT))
			accuracyValue *= 1.02;
		
		// total pp calculation
		double finalMultiplier = 1.12;
		
		if (beatmap.getMods().has(Mod.NO_FAIL))
			finalMultiplier *= 0.9;
		
		if (beatmap.getMods().has(Mod.SPUN_OUT))
			finalMultiplier *= 0.95;
		
		double performance = Math.pow(Math.pow(aimValue, 1.1) + Math.pow(speedValue, 1.1) + Math.pow(accuracyValue, 1.1),1/1.1) * finalMultiplier;
		
		return new Performance(realAccuracy, performance, aimValue, speedValue, accuracyValue); 
	}
	
	private double baseStrain(double strain) {
		return Math.pow(5*Math.max(1, strain/0.0675) - 4, 3)/100000;
	}
	
	private double calculateAccuracy(int num300, int num100, int num50, int numMiss) {
		int total = num300 + num100 + num50 + numMiss;
		if (total > 0)
			return (num300*300 + num100*100 + num50*50)/((double)total*300);
		return 0;
	}
}
