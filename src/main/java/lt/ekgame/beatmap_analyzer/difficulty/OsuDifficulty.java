package lt.ekgame.beatmap_analyzer.difficulty;

import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuCircle;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class OsuDifficulty extends Difficulty<OsuBeatmap> {
	
	private double aimDiff, speedDiff;
	
	public OsuDifficulty(OsuBeatmap beatmap, Mods mods, double starDiff, double aimDiff, double speedDiff) {
		super(beatmap, mods, starDiff);
		this.aimDiff = aimDiff;
		this.speedDiff = speedDiff;
	}

	public double getAim() {
		return aimDiff;
	}

	public double getSpeed() {
		return speedDiff;
	}
	
	public double getAR() {
		return beatmap.getAR(mods);
	}

	public double getOD() {
		return beatmap.getOD(mods);
	}
	
	public int getNumCircles() {
		return (int) beatmap.getHitObjects().stream().filter(o->o instanceof OsuCircle).count();
	}
}
