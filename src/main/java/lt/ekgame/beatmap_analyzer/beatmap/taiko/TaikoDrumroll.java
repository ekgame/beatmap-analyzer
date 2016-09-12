package lt.ekgame.beatmap_analyzer.beatmap.taiko;

import lt.ekgame.beatmap_analyzer.beatmap.HitObject;
import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class TaikoDrumroll extends TaikoObject {

	public TaikoDrumroll(Vec2 position, int startTime, int endTime, int hitSound) {
		super(position, startTime, endTime, hitSound);
	}

	@Override
	public HitObject clone() {
		return new TaikoDrumroll(position.clone(), startTime, endTime, hitSound);
	}
}
