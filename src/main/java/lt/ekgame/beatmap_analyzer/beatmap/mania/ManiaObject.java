package lt.ekgame.beatmap_analyzer.beatmap.mania;

import lt.ekgame.beatmap_analyzer.beatmap.HitObject;
import lt.ekgame.beatmap_analyzer.utils.Vec2;

public abstract class ManiaObject extends HitObject {

	public ManiaObject(Vec2 position, int startTime, int endTime, int hitSound) {
		super(position, startTime, endTime, hitSound);
	}

}
