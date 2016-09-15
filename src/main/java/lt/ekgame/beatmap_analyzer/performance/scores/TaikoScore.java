package lt.ekgame.beatmap_analyzer.performance.scores;

import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;

public class TaikoScore implements Score {
	
	private int combo, numGreat, numHalf, numMiss;
	
	private TaikoScore(int combo, int numGreat, int numHalf, int numMiss) {
		this.combo = combo;
		this.numGreat = numGreat;
		this.numHalf = numHalf;
		this.numMiss = numMiss;
	}
	
	public int getCombo() {
		return combo;
	}

	public int getMisses() {
		return numMiss;
	}
	
	public double getAccuracy() {
		return MathUtils.calculateTaikoAccuracy(numGreat, numHalf, numMiss);
	}

	public static ScoreBuilder of(TaikoBeatmap beatmap) {
		return new ScoreBuilder(beatmap);
	}
	
	public static class ScoreBuilder {
		
		private TaikoBeatmap beatmap;
		private int combo;
		private int numGreat, numHalf, numMiss;
		
		ScoreBuilder(TaikoBeatmap beatmap) {
			this.beatmap = beatmap;
			this.combo = beatmap.getMaxCombo();
		}
		
		public ScoreBuilder combo(int combo) {
			this.combo = combo;
			return this;
		}
		
		public ScoreBuilder accuracy(int numHalf) {
			return accuracy(numHalf, 0);
		}
		
		public ScoreBuilder accuracy(int numHalf, int numMiss) {
			this.numHalf = numHalf;
			this.numMiss = numMiss;
			this.numGreat = beatmap.getMaxCombo() - numHalf - numMiss;
			return this;
		}
		
		public TaikoScore build() {
			return new TaikoScore(combo, numGreat, numHalf, numMiss);
		}
	}
}
