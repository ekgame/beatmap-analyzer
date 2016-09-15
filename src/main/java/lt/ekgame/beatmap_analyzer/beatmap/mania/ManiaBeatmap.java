package lt.ekgame.beatmap_analyzer.beatmap.mania;

import java.util.List;

import lt.ekgame.beatmap_analyzer.Gamemode;
import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapDifficulties;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapEditorState;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapGenerals;
import lt.ekgame.beatmap_analyzer.beatmap.BeatmapMetadata;
import lt.ekgame.beatmap_analyzer.beatmap.BreakPeriod;
import lt.ekgame.beatmap_analyzer.beatmap.TimingPoint;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.difficulty.Difficulty;
import lt.ekgame.beatmap_analyzer.difficulty.ManiaDifficultyCalculator;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class ManiaBeatmap extends Beatmap {
	
	private List<ManiaObject> hitObjects;

	public ManiaBeatmap(BeatmapGenerals generals, BeatmapEditorState editorState, BeatmapMetadata metadata,
			BeatmapDifficulties difficulties, List<BreakPeriod> breaks, List<TimingPoint> timingPoints,
			List<ManiaObject> hitObjects) {
		super(generals, editorState, metadata, difficulties, breaks, timingPoints);
		this.hitObjects = hitObjects;
		
		finalizeObjects(hitObjects);
	}

	@Override
	public Gamemode getGamemode() {
		return Gamemode.MANIA;
	}

	@Override
	public Difficulty<ManiaBeatmap> getDifficulty(Mods mods) {
		return new ManiaDifficultyCalculator().calculate(mods, this);
	}
	
	@Override
	public Difficulty<ManiaBeatmap> getDifficulty() {
		return getDifficulty(Mods.NOMOD);
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
	public int getObjectCount() {
		return hitObjects.size();
	}
}
