package lt.ekgame.beatmap_analyzer.beatmap.taiko;

import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.TaikoDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.performance.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.performance.TaikoPerformanceCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TaikoBeatmap extends Beatmap {
	
	private List<TaikoObject> hitObjects;
	private Difficulty difficulty;
	private TaikoPerformanceCalculator performanceCalculator;
	
	public TaikoBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints, 
			List<TaikoObject> hitObjects) {
		this(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, Mods.NOMOD);
	}

	public TaikoBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints, 
			List<TaikoObject> hitObjects, Mods mods) {
		super(generals, editorState, metadata, difficulties, breaks, timingPoints, mods);
		this.hitObjects = hitObjects;
		finalizeObjects(hitObjects);
	}

	@Override
	public Gamemode getGamemode() {
		return Gamemode.TAIKO;
	}

	@Override
	public Difficulty getDifficulty() {
		if (difficulty == null)
			difficulty = new TaikoDifficultyCalculator(this).calculate();
		return difficulty;
	}

	@Override
	public PerformanceCalculator getPerformanceCalculator() {
		if (performanceCalculator == null)
			performanceCalculator = new TaikoPerformanceCalculator(this);
		return performanceCalculator;
	}

	@Override
	public int getMaxCombo() {
		return (int) hitObjects.stream().filter(o->o instanceof TaikoCircle).count();
	}
	
	public List<TaikoObject> getHitObjects() {
		return hitObjects;
	}

	@Override
	public TaikoBeatmap withMods(Mods mods) {
		if (!this.mods.isNomod())
			throw new IllegalStateException("This beatmap already has mods applied to it.");

		BeatmapGenerals generals = this.generals.clone();
		BeatmapEditorState editorState = this.editorState.clone();
		BeatmapMetadata metadata = this.metadata.clone();
		BeatmapDifficulties difficulties = this.difficulties.clone();

		List<TaikoObject> hitObjects = this.hitObjects.stream().map(o -> (TaikoObject) o.clone()).collect(Collectors.toList());
		List<BreakPeriod> breaks = this.breaks.stream().map(o -> o.clone()).collect(Collectors.toList());
		List<TimingPoint> timingPoints = this.timingPoints.stream().map(o -> o.clone()).collect(Collectors.toList());

		return new TaikoBeatmap(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, mods);
	}

}
