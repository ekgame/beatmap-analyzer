package lt.ekgame.beatmap_analyzer.performance;

import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.performance.scores.Score;

public interface PerformanceCalculator<D extends Difficulty<?, ?>, S extends Score> {
	
	public Performance calculate(D difficulty, S score);

}
