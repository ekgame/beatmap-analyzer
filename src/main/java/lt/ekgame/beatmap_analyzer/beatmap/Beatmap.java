package lt.ekgame.beatmap_analyzer.beatmap;

import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuSlider;
import lt.ekgame.beatmap_analyzer.calculator.Difficulty;
import lt.ekgame.beatmap_analyzer.calculator.DifficultyCalculator;
import lt.ekgame.beatmap_analyzer.calculator.Performance;
import lt.ekgame.beatmap_analyzer.calculator.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class Beatmap {
	
	private final double 
		ODMinMs = 79.5, // OD 0
		ODMaxMs = 19.5, // OD 10
		ARMinMs = 1800, // AR 0
		ARMidMs = 1200, // AR 5
		ARMaxMs = 450;  // AR 10

	private final double 
		ODStep = 6,
		ARStepLow = 120,  // AR 0-5
		ARStepHigh = 150; // AR 5-10
	
	private BeatmapGenerals generals;
	private BeatmapEditorState editorState;
	private BeatmapMetadata metadata;
	private BeatmapDifficulties difficulties;
	
	private List<BreakPeriod> breaks;
	private List<HitObject> hitObjects;
	private List<TimingPoint> timingPoints;
	
	private int maxCombo = -1;
	private Difficulty difficulty = null;
	private PerformanceCalculator performanceCalculator = null;
	private Mods mods = Mods.NOMOD;
	private Gamemode gamemode;

	public Beatmap(BeatmapGenerals generals, BeatmapEditorState editorState,
		BeatmapMetadata metadata, BeatmapDifficulties difficulties,
		List<BreakPeriod> breaks, List<HitObject> hitObjects, List<TimingPoint> timingPoints) 
	{
		this.generals = generals;
		this.editorState = editorState;
		this.metadata = metadata;
		this.difficulties = difficulties;
		this.breaks = breaks;
		this.hitObjects = hitObjects;
		this.timingPoints = timingPoints;
	}
	
	public Beatmap(BeatmapGenerals generals, BeatmapEditorState editorState,
		BeatmapMetadata metadata, BeatmapDifficulties difficulties,
		List<BreakPeriod> breaks, List<HitObject> hitObjects, List<TimingPoint> timingPoints, Mods mods) 
	{
		this(generals, editorState, metadata, difficulties, breaks, hitObjects, timingPoints);
		this.mods = mods;
	}
	
	public PerformanceCalculator getPerformanceCalculator() {
		if (performanceCalculator == null)
			performanceCalculator = new PerformanceCalculator(this);
		return performanceCalculator;
	}
	
	public Performance getPerformance(int combo, double accuracy, int misses) {
		return getPerformanceCalculator().calculate(combo, accuracy, misses, ScoreVersion.VERSION_1);
	}
	
	public Performance getPerformance(int combo, int num100, int num50, int misses) {
		return getPerformanceCalculator().calculate(combo, num100, num50, misses, ScoreVersion.VERSION_1);
	}
	
	public Performance getPerformance(double accuracy) {
		return getPerformanceCalculator().calculate(getMaxCombo(), accuracy, 0, ScoreVersion.VERSION_1);
	}
	
	public Performance getPerformance(int num100, int num50) {
		return getPerformanceCalculator().calculate(getMaxCombo(), num100, num50, 0, ScoreVersion.VERSION_1);
	}
	
	public Performance getPerformance() {
		return getPerformanceCalculator().calculate(getMaxCombo(), 0, 0, 0, ScoreVersion.VERSION_1);
	}
	
	public Mods getMods() {
		return mods;
	}
	
	public int getMaxCombo() {
		if (maxCombo < 0)
			maxCombo = hitObjects.stream()
				.mapToInt(o->(o instanceof OsuSlider) ? ((OsuSlider)o).getCombo() : 1)
				.sum();
		return maxCombo;
	}
	
	public Difficulty getDifficulty() {
		if (difficulty == null)
			difficulty = new DifficultyCalculator(this).calculate();
		return difficulty;
	}

	public BeatmapGenerals getGenerals() {
		return generals;
	}

	public BeatmapEditorState getEditorState() {
		return editorState;
	}

	public BeatmapMetadata getMetadata() {
		return metadata;
	}

	public BeatmapDifficulties getDifficultySettings() {
		return difficulties;
	}

	public List<BreakPeriod> getBreaks() {
		return breaks;
	}

	public List<HitObject> getHitObjects() {
		return hitObjects;
	}
	
	public List<TimingPoint> getTimingPoints() {
		return timingPoints;
	}
	
	public Beatmap withMods(Mods mods) {
		if (!this.mods.isNomod())
			throw new IllegalStateException("This beatmap already has mods applied to it.");
		
		BeatmapGenerals generals = this.generals.clone();
		BeatmapEditorState editorState = this.editorState.clone();
		BeatmapMetadata metadata = this.metadata.clone();
		BeatmapDifficulties difficulties = this.difficulties.clone();
		
		List<HitObject> hitObjects = this.hitObjects.stream()
			.map(o->o.clone()).collect(Collectors.toList());
		List<BreakPeriod> breaks = this.breaks.stream()
			.map(o->o.clone()).collect(Collectors.toList());
		List<TimingPoint> timingPoints = this.timingPoints.stream()
			.map(o->o.clone()).collect(Collectors.toList());
		
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
			
			double overallDifficulty = difficulties.getOD()*odMultiplier;
			double overallDifficultyTime = ODMinMs - Math.ceil(ODStep*overallDifficulty);
			overallDifficultyTime = MathUtils.clamp(ODMaxMs, ODMinMs, overallDifficultyTime/speedMultiplier);
			overallDifficulty = (ODMinMs - overallDifficultyTime)/ODStep;
			
			double approachRate = difficulties.getAR()*arMultiplier;
			double approachRateTime = approachRate <= 5 ? (ARMinMs - ARStepLow*approachRate) : (ARMidMs - ARStepHigh*(approachRate - 5));
			approachRateTime = MathUtils.clamp(ARMaxMs, ARMinMs, approachRateTime/speedMultiplier);
			approachRate = approachRate <= 5 ? ((ARMinMs - approachRateTime)/ARStepLow) : (5 + (ARMidMs - approachRateTime)/ARStepHigh);
			
			double circleSize = difficulties.getCS()*csMultiplier;
			circleSize = MathUtils.clamp(0, 10, circleSize);
			
			difficulties.setAR(approachRate);
			difficulties.setOD(overallDifficulty);
			difficulties.setCS(circleSize);
			
			if (mods.isSpeedChanging()) {
				for (HitObject object : hitObjects) {
					object.setStartTime((int) (object.getStartTime()/speedMultiplier));
					object.setEndTime((int) (object.getEndTime()/speedMultiplier));
				}
				
				for (TimingPoint point : timingPoints) {
					point.setTimestamp((int) (point.getTimestamp()/speedMultiplier));
					if (!point.isInherited())
						point.setBeatLength(point.getBeatLength()/speedMultiplier);
				}
				
				for (BreakPeriod breakPeriod : breaks) {
					breakPeriod.setStartTime((int) (breakPeriod.getStartTime()/speedMultiplier));
					breakPeriod.setEndTime((int) (breakPeriod.getEndTime()/speedMultiplier));
				}
			}
		}
		
		BeatmapParser.calculateSliderEnds(hitObjects, timingPoints, difficulties.getSliderMultiplier(), difficulties.getTickRate());
		
		return new Beatmap(generals, editorState, metadata, difficulties, breaks, hitObjects, timingPoints, mods);
	}
}
