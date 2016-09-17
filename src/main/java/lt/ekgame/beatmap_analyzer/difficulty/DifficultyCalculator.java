package lt.ekgame.beatmap_analyzer.difficulty;

import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.performance.scores.Score;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public interface DifficultyCalculator<T extends Beatmap, S extends Score> {
	
	public Difficulty<T, S> calculate(Mods mods, T beatmap);

}
