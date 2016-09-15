package lt.ekgame.beatmap_analyzer.difficulty;

import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public interface DifficultyCalculator<T extends Beatmap> {
	
	public Difficulty<T> calculate(Mods mods, T beatmap);

}
