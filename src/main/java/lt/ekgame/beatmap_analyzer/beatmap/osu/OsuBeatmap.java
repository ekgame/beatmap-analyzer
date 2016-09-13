package lt.ekgame.beatmap_analyzer.beatmap.osu;

import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.*;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.performance.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class OsuBeatmap extends Beatmap {
	
	private List<OsuObject> hitObjects;

	private Difficulty difficulty = null;
	private PerformanceCalculator performanceCalculator = null;

	public OsuBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
			List<OsuObject> hitObjects) {
		this(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, Mods.NOMOD);
	}

	public OsuBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
			List<OsuObject> hitObjects, Mods mods) 
	{
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
			
			// TODO: use time rate instead of messing with object timings (produces more accurate results)
			if (mods.isSpeedChanging())
				applySpeedChange(hitObjects, speedMultiplier);
		}
		
		finalizeObjects(hitObjects);
	}

	@Override
	public Gamemode getGamemode() {
		return Gamemode.OSU;
	}
	
	@Override
	public int getMaxCombo() {
		return hitObjects.stream().mapToInt(o->o.getCombo()).sum();
	}

	public List<OsuObject> getHitObjects() {
		return hitObjects;
	}

	@Override
	public Difficulty getDifficulty() {
		if (difficulty == null)
			difficulty = new OsuDifficultyCalculator(this).calculate();
		return difficulty;
	}

	@Override
	public PerformanceCalculator getPerformanceCalculator() {
		if (performanceCalculator == null)
			performanceCalculator = new PerformanceCalculator(this);
		return performanceCalculator;
	}

	@Override
	public OsuBeatmap withMods(Mods mods) {
		if (!this.mods.isNomod())
			throw new IllegalStateException("This beatmap already has mods applied to it.");

		BeatmapGenerals generals = this.generals.clone();
		BeatmapEditorState editorState = this.editorState.clone();
		BeatmapMetadata metadata = this.metadata.clone();
		BeatmapDifficulties difficulties = this.difficulties.clone();

		List<OsuObject> hitObjects = this.hitObjects.stream().map(o -> (OsuObject) o.clone()).collect(Collectors.toList());
		List<BreakPeriod> breaks = this.breaks.stream().map(o -> o.clone()).collect(Collectors.toList());
		List<TimingPoint> timingPoints = this.timingPoints.stream().map(o -> o.clone()).collect(Collectors.toList());

		return new OsuBeatmap(generals, editorState, metadata, difficulties, breaks, timingPoints, hitObjects, mods);
	}
}
