package lt.ekgame.beatmap_analyzer.difficulty;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.performance.ManiaPerformanceCalculator;
import lt.ekgame.beatmap_analyzer.performance.Performance;
import lt.ekgame.beatmap_analyzer.performance.scores.ManiaScore;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class ManiaDifficulty extends Difficulty<ManiaBeatmap, ManiaScore> {

	public ManiaDifficulty(ManiaBeatmap beatmap, Mods mods, double starDiff) {
		super(beatmap, mods, starDiff);
	}

	@Override
	public Performance getPerformance(ManiaScore score) {
		return new ManiaPerformanceCalculator().calculate(this, score);
	}
}
