package lt.ekgame.beatmap_analyzer.performance.scores;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;

public class ManiaScore implements Score {
	
	private int score;
	private double accuracy;
	
	private ManiaScore(int score, double accuracy) {
		this.score = score;
		this.accuracy = accuracy;
	}
	
	public int getScore() {
		return score;
	}
	
	public double getAccuracy() {
		return accuracy;
	}

	public static ScoreBuilder of(ManiaBeatmap beatmap) {
		return new ScoreBuilder(beatmap);
	}
	
	public static class ScoreBuilder {
		
		private ManiaBeatmap beatmap;
		private int score = 1_000_000;
		private double accuracy = 1;
		
		ScoreBuilder(ManiaBeatmap beatmap) {
			this.beatmap = beatmap;
		}
		
		public ScoreBuilder score(int score) {
			this.score = score;
			return this;
		}
		
		public ScoreBuilder accuracy(int num200, int num100) {
			return accuracy(num200, num100, 0, 0);
		}
		
		public ScoreBuilder accuracy(int num200, int num100, int num50) {
			return accuracy(num200, num100, num50, 0);
		}
		
		public ScoreBuilder accuracy(int num200, int num100, int num50, int numMiss) {
			int num300 = beatmap.getObjectCount() - num200 - num100 - num50 - numMiss;
			return accuracy(MathUtils.calculateManiaAccuracy(0, num300, num200, num100, num50, numMiss));
		}
		
		public ScoreBuilder accuracy(double accuracy) {
			this.accuracy = accuracy;
			return this;
		}
		
		public ManiaScore build() {
			return new ManiaScore(score, accuracy);
		}
	}
}
