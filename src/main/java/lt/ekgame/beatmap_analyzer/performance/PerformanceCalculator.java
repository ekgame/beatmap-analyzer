package lt.ekgame.beatmap_analyzer.performance;

import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public abstract class PerformanceCalculator {
	
	public Performance calculate(int combo, double accuracy, int misses, ScoreVersion version) {
		misses = Math.min(getObjectCount(), misses);
		int max300 = getObjectCount() - misses;
		
		accuracy = Math.max(0.0, Math.min(calculateAccuracy(max300, 0, 0, misses) * 100, accuracy));
		int num50 = 0;
		int num100 = (int)Math.round(-3*((accuracy - 1)* getObjectCount() + misses)*0.5);
		
		if (num100 > getObjectCount() - misses) {
			num100 = 0;
			num50 = (int) Math.round(-6.0*((accuracy - 1)*getObjectCount() + misses)*0.2);
			num50 = Math.min(max300, num50);
		}
		else {
			num100 = Math.min(max300, num100);
		}
		return calculate(combo, num100, num50, misses, version);
	}
	
	public abstract Performance calculate(int combo, int num100, int num50, int misses, ScoreVersion version);
	
	public abstract int getObjectCount();
	
	protected double calculateAccuracyCombo(int maxCombo, int num100, int num50, int numMiss) {
		int num300 = maxCombo - num100 - num50 - numMiss;
		return calculateAccuracy(num300, num100, num50, numMiss);
	}
	
	protected double calculateAccuracy(int num300, int num100, int num50, int numMiss) {
		int total = num300 + num100 + num50 + numMiss;
		if (total > 0)
			return (num300*300 + num100*100 + num50*50)/((double)total*300);
		return 0;
	}
}
