package lt.ekgame.beatmap_analyzer.beatmap.taiko;

import java.util.List;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficulty;
import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.difficulty.TaikoDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.performance.OsuPerformanceCalculator;
import lt.ekgame.beatmap_analyzer.performance.Performance;
import lt.ekgame.beatmap_analyzer.performance.TaikoPerformanceCalculator;
import lt.ekgame.beatmap_analyzer.performance.scores.OsuScore;
import lt.ekgame.beatmap_analyzer.performance.scores.TaikoScore;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TaikoBeatmap extends Beatmap {
	
	private List<TaikoObject> hitObjects;

	public TaikoBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints, 
			List<TaikoObject> hitObjects) {
		super(generals, editorState, metadata, difficulties, breaks, timingPoints);
		this.hitObjects = hitObjects;
		finalizeObjects(hitObjects);
	}

	@Override
	public Gamemode getGamemode() {
		return Gamemode.TAIKO;
	}

	@Override
	public Difficulty<TaikoBeatmap> getDifficulty(Mods mods) {
		return new TaikoDifficultyCalculator().calculate(mods, this);
	}

	@Override
	public Difficulty<TaikoBeatmap> getDifficulty() {
		return getDifficulty(Mods.NOMOD);
	}

	@Override
	public int getMaxCombo() {
		return (int) hitObjects.stream().filter(o->o instanceof TaikoCircle).count();
	}
	
	public List<TaikoObject> getHitObjects() {
		return hitObjects;
	}

	@Override
	public int getObjectCount() {
		return hitObjects.size();
	}
	
	public Performance getPerformance(TaikoScore score, Mods mods) {
		return new TaikoPerformanceCalculator().calculate(getDifficulty(mods), score);
	}
	
	public Performance getPerformance(TaikoScore score, Mod... mods) {
		return getPerformance(score, new Mods(mods));
	}
}
