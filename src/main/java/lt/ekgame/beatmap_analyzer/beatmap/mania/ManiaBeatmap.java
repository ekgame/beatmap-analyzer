package lt.ekgame.beatmap_analyzer.beatmap.mania;

import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapDifficulties;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapEditorState;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapGenerals;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapMetadata;
import lt.ekgame.beatmap_analyzer.beatmap.BreakPeriod;
import lt.ekgame.beatmap_analyzer.beatmap.TimingPoint;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoObject;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.ManiaDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.performance.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class ManiaBeatmap extends Beatmap {
	
	private List<ManiaObject> hitObjects;
	private Difficulty difficulty;
	
	public ManiaBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
			List<ManiaObject> hitObjects) {
		this(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, Mods.NOMOD);
	}

	public ManiaBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
			List<ManiaObject> hitObjects, Mods mods) {
		super(generals, editorState, metadata, difficulties, breaks, timingPoints, mods);
		this.hitObjects = hitObjects;
		
		finalizeObjects(hitObjects);
	}

	@Override
	public Gamemode getGamemode() {
		return Gamemode.MANIA;
	}

	@Override
	public Difficulty getDifficulty() {
		if (difficulty == null)
			difficulty = new ManiaDifficultyCalculator(this).calculate();
		return difficulty;
	}

	@Override
	public PerformanceCalculator getPerformanceCalculator() {
		return null;
	}
	
	public List<ManiaObject> getHitObjects() {
		return hitObjects;
	}
	
	public int getCollumns() {
		return (int)difficulties.getCS();
	}

	@Override
	public int getMaxCombo() {
		return hitObjects.stream().mapToInt(o->o.getCombo()).sum();
	}

	@Override
	public ManiaBeatmap withMods(Mods mods) {
		if (!this.mods.isNomod())
			throw new IllegalStateException("This beatmap already has mods applied to it.");

		return new ManiaBeatmap(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, mods);
	}

}
