package lt.ekgame.beatmap_analyzer.performance.scores;

import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class OsuScore implements Score {
	
	private int combo = -1;
	private int num300, num100, num50, numMiss;
	private ScoreVersion version = ScoreVersion.VERSION_1;
	
	private OsuScore(int combo, int num300, int num100, int num50, int numMiss, ScoreVersion version) {
		this.combo = combo;
		this.num300 = num300;
		this.num100 = num100;
		this.num50 = num50;
		this.numMiss = numMiss;
		this.version = version;
	}
	
	public double getScoreV1Accuracy(int circles) {
		double realAccuracy = 0;
		if (circles > 0)
			realAccuracy = ((num300 - (getTotalHits() - circles))*300 + num100*100 + num50*50)/((double)circles*300);
		return Math.max(0, realAccuracy);
	}
	
	public double getAccuracy() {
		return MathUtils.calculateOsuAccuracy(num300, num100, num50, numMiss);
	}
	
	public int getTotalHits() {
		return num300 + num100 + num50 + numMiss;
	}
	
	public int getCombo() {
		return combo;
	}

	public int get300s() {
		return num300;
	}
	
	public int get100s() {
		return num100;
	}

	public int get50s() {
		return num50;
	}

	public int getMisses() {
		return numMiss;
	}

	public ScoreVersion getScoreVersion() {
		return version;
	}

	public static ScoreBuilder of(OsuBeatmap beatmap) {
		return new ScoreBuilder(beatmap);
	}
	
	public static class ScoreBuilder {
		
		private OsuBeatmap beatmap;
		private int combo;
		private int num300, num100, num50, numMiss;
		private ScoreVersion version = ScoreVersion.VERSION_1;
		
		ScoreBuilder(OsuBeatmap beatmap) {
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
		
		public ScoreBuilder accuracy(double accuracy, int misses) {
			numMiss = Math.min(beatmap.getObjectCount(), misses);
			int max300 = beatmap.getObjectCount() - numMiss;
			
			accuracy = Math.max(0.0, Math.min(MathUtils.calculateOsuAccuracy(max300, 0, 0, numMiss), accuracy));
			num50 = 0;
			num100 = (int)Math.round(-3*((accuracy - 1)*beatmap.getObjectCount() + numMiss)*0.5);
			
			if (num100 > beatmap.getObjectCount() - numMiss) {
				num100 = 0;
				num50 = (int) Math.round(-6.0*((accuracy - 1)*beatmap.getObjectCount() + numMiss)*0.2);
				num50 = Math.min(max300, num50);
			}
			else {
				num100 = Math.min(max300, num100);
			}
			num300 = beatmap.getObjectCount() - num100 - num50 - numMiss;
			return this;
		}
		
		public ScoreBuilder accuracy(int num100, int num50) {
			return accuracy(num100, num50, 0);
		}
		
		public ScoreBuilder accuracy(int num100, int num50, int numMiss) {
			this.num100 = num100;
			this.num50 = num50;
			this.numMiss = numMiss;
			this.num300 = beatmap.getObjectCount() - num100 - num50 - numMiss;
			return this;
		}
		
		public ScoreBuilder version(ScoreVersion version) {
			this.version = version;
			return this;
		}
		
		public OsuScore build() {
			return new OsuScore(combo, num300, num100, num50, numMiss, version);
		}
	}
}
