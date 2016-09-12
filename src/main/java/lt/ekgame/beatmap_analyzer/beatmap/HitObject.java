package lt.ekgame.beatmap_analyzer.beatmap;

import lt.ekgame.beatmap_analyzer.utils.Vec2;

public abstract class HitObject {

	protected Vec2 position;
	protected int startTime, endTime;
	protected int hitSound;
	protected boolean isNewCombo;
	
	public HitObject(Vec2 position, int startTime, int endTime, int hitSound, boolean isNewCombo) {
		this.position = position;
		this.startTime = startTime;
		this.endTime = endTime;
		this.hitSound = hitSound;
		this.isNewCombo = isNewCombo;
	}
	
	public abstract HitObject clone();

	public Vec2 getPosition() {
		return position;
	}

	public int getStartTime() {
		return startTime;
	}
	
	public int getEndTime() {
		return endTime;
	}

	public int getHitSound() {
		return hitSound;
	}

	public boolean isNewCombo() {
		return isNewCombo;
	}

	public void setPosition(Vec2 position) {
		this.position = position;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(int endTime) {
		this.endTime = endTime;
	}

	public void setHitSound(int hitSound) {
		this.hitSound = hitSound;
	}

	public void setNewCombo(boolean isNewCombo) {
		this.isNewCombo = isNewCombo;
	}
}