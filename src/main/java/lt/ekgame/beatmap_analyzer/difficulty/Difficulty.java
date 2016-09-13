package lt.ekgame.beatmap_analyzer.difficulty;

public class Difficulty {
	
	private double aimDifficulty, speedDifficulty, starDifficulty;
	
	public Difficulty(double aimDifficulty, double speedDifficulty, double starDifficulty) {
		this.aimDifficulty = aimDifficulty;
		this.speedDifficulty = speedDifficulty;
		this.starDifficulty = starDifficulty;
	}

	public double getAimDifficulty() {
		return aimDifficulty;
	}

	public double getSpeedDifficulty() {
		return speedDifficulty;
	}

	public double getStarDifficulty() {
		return starDifficulty;
	}
}
