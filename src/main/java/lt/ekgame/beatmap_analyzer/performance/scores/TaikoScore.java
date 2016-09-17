package lt.ekgame.beatmap_analyzer.performance.scores;

import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;

public class TaikoScore implements Score {
	
	private int combo, numMiss;
	private double accuracy;
	
	private TaikoScore(int combo, double accuracy, int numMiss) {
		this.combo = combo;
		this.accuracy = accuracy;
		this.numMiss = numMiss;
	}
	
	public int getCombo() {
		return combo;
	}

	public int getMisses() {
		return numMiss;
	}
	
	public double getAccuracy() {
		return accuracy;
	}

	public static ScoreBuilder of(TaikoBeatmap beatmap) {
		return new ScoreBuilder(beatmap);
	}
	
	public static class ScoreBuilder {
		
		private TaikoBeatmap beatmap;
		private int combo, numMiss;
		private double accuracy;
		
		ScoreBuilder(TaikoBeatmap beatmap) {
			this.beatmap = beatmap;
			this.combo = beatmap.getMaxCombo();
		}
		
		public ScoreBuilder combo(int combo) {
			this.combo = combo;
			return this;
		}
		
		public ScoreBuilder accuracy(double accuracy) {
			return accuracy(accuracy, 0);
		}
		
		public ScoreBuilder accuracy(double accuracy, int numMiss) {
			this.numMiss = numMiss;
			this.accuracy = accuracy;
			return this;
		}
		
		public ScoreBuilder accuracy(int numHalf) {
			return accuracy(numHalf, 0);
		}
		
		public ScoreBuilder accuracy(int numHalf, int numMiss) {
			this.numMiss = numMiss;
			int numGreat = beatmap.getMaxCombo() - numHalf - numMiss;
			accuracy = MathUtils.calculateTaikoAccuracy(numGreat, numHalf, numMiss);
			return this;
		}
		
		public TaikoScore build() {
			return new TaikoScore(combo, accuracy, numMiss);
		}
	}
}
