package lt.ekgame.beatmap_analyzer.performance;

import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficulty;
import lt.ekgame.beatmap_analyzer.performance.scores.OsuScore;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class OsuPerformanceCalculator implements PerformanceCalculator<OsuDifficulty, OsuScore> {
	
	public Performance calculate(OsuDifficulty difficulty, OsuScore score) {
		if (difficulty.getMaxCombo() <= 0)
			throw new IllegalStateException("Beatmap must have elements.");
		
		double hitsOver2k = score.getTotalHits()/(double)2000;
		double lengthBonus = 0.95 + 0.4*Math.min(1, hitsOver2k) + (score.getTotalHits() > 2000 ? Math.log10(hitsOver2k)*0.5 : 0);
		double missPenalty = Math.pow(0.97, score.getMisses());
		double comboBreak = Math.pow(score.getCombo(), 0.8)/Math.pow(difficulty.getMaxCombo(), 0.8);
		
		// aim calculation
		double ARBonus = 1;
		double approachRate = difficulty.getAR();
		if (approachRate > 10.33) {
			ARBonus += 0.45*(approachRate - 10.33);
		}
		else if (approachRate < 8) {
			double lowARBonus = 0.01*(8 - approachRate);
			if (difficulty.hasMod(Mod.HIDDEN))
				lowARBonus *= 2;
			ARBonus += lowARBonus;
		}
		
		double aimValue = baseStrain(difficulty.getAim());
		aimValue *= lengthBonus*missPenalty*comboBreak*ARBonus;
		
		if (difficulty.hasMod(Mod.HIDDEN))
			aimValue *= 1.18;
		
		if (difficulty.hasMod(Mod.FLASHLIGHT))
			aimValue *= 1.45*lengthBonus;
		
		double accuracy = score.getAccuracy();
		double accuracyBonus = 0.5 + accuracy/2;
		double ODBonus = 0.98 + Math.pow(difficulty.getOD(), 2)/2500;
		
		aimValue *= accuracyBonus*ODBonus;
		
		// speed calculation
		double speedValue = baseStrain(difficulty.getSpeed());
		speedValue *= lengthBonus*missPenalty*comboBreak*accuracyBonus*ODBonus;
		
		// score v2
		double realAccuracy = accuracy;
		int circles = score.getTotalHits();
		
		// score v1
		if (score.getScoreVersion() == ScoreVersion.VERSION_1) {
			circles = difficulty.getNumCircles();
			realAccuracy = score.getScoreV1Accuracy(circles);
		}
		
		// accuracy calculation
		double accuracyValue = Math.pow(1.52163, difficulty.getOD())*Math.pow(realAccuracy, 24.0) * 2.83;
		accuracyValue *= Math.min(1.15, Math.pow(circles/(double)1000, 0.3));
		
		if (difficulty.hasMod(Mod.HIDDEN))
			accuracyValue *= 1.02;
		
		if (difficulty.hasMod(Mod.FLASHLIGHT))
			accuracyValue *= 1.02;
		
		// total pp calculation
		double finalMultiplier = 1.12;
		
		if (difficulty.hasMod(Mod.NO_FAIL))
			finalMultiplier *= 0.9;
		
		if (difficulty.hasMod(Mod.SPUN_OUT))
			finalMultiplier *= 0.95;
		
		double performance = Math.pow(Math.pow(aimValue, 1.1) + Math.pow(speedValue, 1.1) + Math.pow(accuracyValue, 1.1),1/1.1) * finalMultiplier;
		
		return new Performance(realAccuracy, performance, aimValue, speedValue, accuracyValue); 
	}
	
	private double baseStrain(double strain) {
		return Math.pow(5*Math.max(1, strain/0.0675) - 4, 3)/100000;
	}
}

