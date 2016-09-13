package lt.ekgame.beatmap_analyzer.beatmap;

import java.util.List;
import java.util.ListIterator;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.performance.Performance;
import lt.ekgame.beatmap_analyzer.performance.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public abstract class Beatmap {
	
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
	
	protected BeatmapGenerals generals;
	protected BeatmapEditorState editorState;
	protected BeatmapMetadata metadata;
	protected BeatmapDifficulties difficulties;
	
	protected List<BreakPeriod> breaks;
	protected List<TimingPoint> timingPoints;
	
	protected Mods mods = Mods.NOMOD;
	
	protected Beatmap(BeatmapGenerals generals, BeatmapEditorState editorState,
		BeatmapMetadata metadata, BeatmapDifficulties difficulties,
		List<BreakPeriod> breaks, List<TimingPoint> timingPoints, Mods mods) 
	{
		this.generals = generals;
		this.editorState = editorState;
		this.metadata = metadata;
		this.difficulties = difficulties;
		this.breaks = breaks;
		this.timingPoints = timingPoints;
		this.mods = mods;
	}
	
	protected void finalizeObjects(List<? extends HitObject> objects) {
		ListIterator<TimingPoint> timingIterator = timingPoints.listIterator();
		ListIterator<? extends HitObject> objectIterator = objects.listIterator();
		
		// find first parent point
		TimingPoint parent = null;
		while (parent == null || parent.isInherited())
			parent = timingIterator.next();
		
		while (true) {
			TimingPoint current = timingIterator.hasNext() ? timingIterator.next() : null;
			TimingPoint previous = timingPoints.get(timingIterator.previousIndex() - (current == null ? 0 : 1));
			if (!previous.isInherited()) parent = previous;
			
			while (objectIterator.hasNext()) {
				HitObject object = objectIterator.next();
				if (current == null || object.getStartTime() < current.getTimestamp()) {
					object.finalize(previous, parent, this);
				}
				else {
					objectIterator.previous();
					break;
				}	
			}
			
			if (current == null) break;
		}
	}
	
	public abstract Gamemode getGamemode();
	
	public abstract Difficulty getDifficulty();
	
	public abstract PerformanceCalculator getPerformanceCalculator();
	
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
	
	public abstract int getMaxCombo();

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
	
	public List<TimingPoint> getTimingPoints() {
		return timingPoints;
	}
	
	protected void applySpeedChange(List<? extends HitObject> objects, double speedMultiplier) {
		for (HitObject object : objects) {
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
	
	protected void applyOverallDifficultyChange(double odMultiplier, double speedMultiplier) {
		double overallDifficulty = difficulties.getOD()*odMultiplier;
		double overallDifficultyTime = ODMinMs - Math.ceil(ODStep*overallDifficulty);
		overallDifficultyTime = MathUtils.clamp(ODMaxMs, ODMinMs, overallDifficultyTime/speedMultiplier);
		overallDifficulty = (ODMinMs - overallDifficultyTime)/ODStep;
		difficulties.setOD(overallDifficulty);
	}
	
	protected void applyApproachRateChange(double arMultiplier, double speedMultiplier) {
		double approachRate = difficulties.getAR()*arMultiplier;
		double approachRateTime = approachRate <= 5 ? (ARMinMs - ARStepLow*approachRate) : (ARMidMs - ARStepHigh*(approachRate - 5));
		approachRateTime = MathUtils.clamp(ARMaxMs, ARMinMs, approachRateTime/speedMultiplier);
		approachRate = approachRate <= 5 ? ((ARMinMs - approachRateTime)/ARStepLow) : (5 + (ARMidMs - approachRateTime)/ARStepHigh);
		difficulties.setAR(approachRate);
	}
	
	protected void applyCircleSizeChange(double csMultiplier) {
		double circleSize = difficulties.getCS()*csMultiplier;
		circleSize = MathUtils.clamp(0, 10, circleSize);
		difficulties.setCS(circleSize);
	}
	
	public abstract Beatmap withMods(Mods mods);
}
