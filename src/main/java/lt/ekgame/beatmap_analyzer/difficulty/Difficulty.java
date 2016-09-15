package lt.ekgame.beatmap_analyzer.difficulty;

import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class Difficulty<T extends Beatmap> {
	
	protected T beatmap;
	protected Mods mods;
	protected double starDiff;
	
	public Difficulty(T beatmap, Mods mods, double starDiff) {
		this.beatmap = beatmap;
		this.mods = mods;
		this.starDiff = starDiff;
	}
	
	public double getSpeedMultiplier() {
		return mods.getSpeedMultiplier();
	}
	
	public double getOD() {
		return beatmap.getDifficultySettings().getOD();
	}
	
	public T getBeatmap() {
		return beatmap;
	}
	
	public Mods getMods() {
		return mods;
	}

	public double getStars() {
		return starDiff;
	}

	public int getMaxCombo() {
		return beatmap.getMaxCombo();
	}
	
	public int getObjectCount() {
		return beatmap.getObjectCount();
	}
	
	public boolean hasMod(Mod mod) {
		return mods.has(mod);
	}
}
