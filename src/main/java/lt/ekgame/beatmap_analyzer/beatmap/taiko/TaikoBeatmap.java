package lt.ekgame.beatmap_analyzer.beatmap.taiko;

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
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuObject;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.TaikoDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.performance.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TaikoBeatmap extends Beatmap {
	
	private List<TaikoObject> hitObjects;
	private Difficulty difficulty;
	
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
		
		if (mods.isMapChanging()) {
			double speedMultiplier = 1;
			if (mods.has(Mod.DOUBLE_TIME) || mods.has(Mod.NIGHTCORE)) speedMultiplier *= 1.5;
			if (mods.has(Mod.HALF_TIME)) speedMultiplier *= 0.75;

			double odMultiplier = 1;
			if (mods.has(Mod.HARDROCK)) odMultiplier *= 1.4;
			if (mods.has(Mod.EASY)) odMultiplier *= 0.5;

			double arMultiplier = 1;
			if (mods.has(Mod.HARDROCK)) arMultiplier *= 1.4;
			if (mods.has(Mod.EASY)) arMultiplier *= 0.5;

			double csMultiplier = 1;
			if (mods.has(Mod.HARDROCK)) csMultiplier *= 1.3;
			if (mods.has(Mod.EASY)) csMultiplier *= 0.5;
			
			applyOverallDifficultyChange(odMultiplier, speedMultiplier);
			applyApproachRateChange(arMultiplier, speedMultiplier);
			applyCircleSizeChange(csMultiplier);
			
			//if (mods.isSpeedChanging())
			//	applySpeedChange(hitObjects, speedMultiplier);
		}
		
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
		return null;
	}

	@Override
	public int getMaxCombo() {
		return 0;
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
