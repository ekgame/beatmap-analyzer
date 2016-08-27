package lt.ekgame.beatmap_analyzer.beatmap.osu;

import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class Spinner extends HitObject {
	
	public Spinner(Vec2 position, int startTime, int endTime, int hitSound, boolean isNewCombo) {
		super(position, startTime, endTime, hitSound, isNewCombo);
	}

	@Override
	public HitObject clone() {
		return new Spinner(position.clone(), startTime, endTime, hitSound, isNewCombo);
	}
}
