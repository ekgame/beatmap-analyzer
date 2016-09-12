package lt.ekgame.beatmap_analyzer.beatmap.taiko;

import lt.ekgame.beatmap_analyzer.beatmap.HitObject;
import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class TaikoCircle extends TaikoObject {
	
	private TaikoColor color;

	public TaikoCircle(Vec2 position, int startTime, int hitSound, TaikoColor color) {
		super(position, startTime, startTime, hitSound);
		this.color = color;
	}

	@Override
	public HitObject clone() {
		return new TaikoCircle(position.clone(), startTime, hitSound, color);
	}
	
	public TaikoColor getColor() {
		return color;
	}

	public enum TaikoColor {
		RED, BLUE
	}
}
