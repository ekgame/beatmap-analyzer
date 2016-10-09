package lt.ekgame.beatmap_analyzer.performance;

import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficulty;
import lt.ekgame.beatmap_analyzer.performance.scores.Score;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class OsuPerformanceCalculator implements PerformanceCalculator {
	
	public Performance calculate(Difficulty difficulty, Score score) {
		OsuDifficulty diff = (OsuDifficulty) difficulty;
		
		if (diff.getMaxCombo() <= 0)
			throw new IllegalStateException("Beatmap must have elements.");
		
		double hitsOver2k = diff.getObjectCount()/(double)2000;
		double lengthBonus = 0.95 + 0.4*Math.min(1, hitsOver2k) + (diff.getObjectCount() > 2000 ? Math.log10(hitsOver2k)*0.5 : 0);
		double missPenalty = Math.pow(0.97, score.getMisses());
		double comboBreak = Math.pow(score.getCombo(), 0.8)/Math.pow(diff.getMaxCombo(), 0.8);
		
		// aim calculation
		double ARBonus = 1;
		double approachRate = diff.getAR();
		if (approachRate > 10.33) {
			ARBonus += 0.45*(approachRate - 10.33);
		}
		else if (approachRate < 8) {
			double lowARBonus = 0.01*(8 - approachRate);
			if (diff.hasMod(Mod.HIDDEN))
				lowARBonus *= 2;
			ARBonus += lowARBonus;
		}
		
		double aimValue = baseStrain(diff.getAim());
		aimValue *= lengthBonus*missPenalty*comboBreak*ARBonus;
		
		if (diff.hasMod(Mod.HIDDEN))
			aimValue *= 1.18;
		
		if (diff.hasMod(Mod.FLASHLIGHT))
			aimValue *= 1.45*lengthBonus;
		
		double accuracy = score.getAccuracy();
		double accuracyBonus = 0.5 + accuracy/2;
		double ODBonus = 0.98 + Math.pow(diff.getOD(), 2)/2500;
		
		aimValue *= accuracyBonus*ODBonus;
		
		// speed calculation
		double speedValue = baseStrain(diff.getSpeed());
		speedValue *= lengthBonus*missPenalty*comboBreak*accuracyBonus*ODBonus;
		
		// score v2
		double realAccuracy = accuracy;
		int circles = diff.getObjectCount();
		
		// score v1
		if (score.getVersion() == ScoreVersion.V1) {
			circles = diff.getNumCircles();
			realAccuracy = getScoreV1Accuracy(diff.getObjectCount(), circles, accuracy, score.getMisses());
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
	
	private double getScoreV1Accuracy(int objects, int circles, double accuracy, int misses) {
		misses = Math.min(objects, misses);
		int max300 = objects - misses;
		
		accuracy = Math.max(0.0, Math.min(MathUtils.calculateOsuAccuracy(max300, 0, 0, misses), accuracy));
		int num50 = 0;
		int num100 = (int)Math.round(-3*((accuracy - 1)*objects + misses)*0.5);
		
		if (num100 > objects - misses) {
			num100 = 0;
			num50 = (int) Math.round(-6.0*((accuracy - 1)*objects + misses)*0.2);
			num50 = Math.min(max300, num50);
		}
		else {
			num100 = Math.min(max300, num100);
		}
		int num300 = objects - num100 - num50 - misses;
		
		double realAccuracy = 0;
		if (circles > 0)
				realAccuracy = ((num300 - (objects - circles))*300 + num100*100 + num50*50)/((double)circles*300);
		return Math.max(0, realAccuracy);
	}
	
	private double baseStrain(double strain) {
		return Math.pow(5*Math.max(1, strain/0.0675) - 4, 3)/100000;
	}
}

