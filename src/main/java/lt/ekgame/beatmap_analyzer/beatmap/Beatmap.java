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
	
	protected void applyOverallDifficultyChange(double multiplier, double speedMultiplier) {
		difficulties.setOD(MathUtils.recalculateOverallDifficulty(difficulties.getOD(), multiplier, speedMultiplier));
	}
	
	protected void applyApproachRateChange(double multiplier, double speedMultiplier) {
		difficulties.setAR(MathUtils.recalculateApproachRate(difficulties.getAR(), multiplier, speedMultiplier));
	}
	
	protected void applyCircleSizeChange(double multiplier) {
		difficulties.setCS(MathUtils.recalculateCircleSize(difficulties.getCS(), multiplier));
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
	
	
	
	public abstract Beatmap withMods(Mods mods);
}
