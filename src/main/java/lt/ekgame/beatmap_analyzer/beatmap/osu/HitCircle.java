package lt.ekgame.beatmap_analyzer.beatmap.osu;

import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class HitCircle extends HitObject {

	public HitCircle(Vec2 position, int timestamp, int hitSound, boolean isNewCombo) {
		super(position, timestamp, timestamp, hitSound, isNewCombo);
	}

	@Override
	public HitObject clone() {
		return new HitCircle(position.clone(), startTime, hitSound, isNewCombo);
	}

}
