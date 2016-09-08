package lt.ekgame.beatmap_analyzer.calculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.Beatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.HitObject;
import lt.ekgame.beatmap_analyzer.beatmap.osu.Spinner;
import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class DifficultyCalculator {
	
	private static final double DECAY_BASE[] = {0.3, 0.15};
	private static final double WEIGHT_SCALING[] = {1400, 26.25};
	private static final double STAR_SCALING_FACTOR = 0.0675;
	private static final double EXTREME_SCALING_FACTOR = 0.5;
	private static final float PLAYFIELD_WIDTH = 512;
	private static final double DECAY_WEIGHT = 0.9;
	
	private static final double ALMOST_DIAMETER = 90;
	private static final double STREAM_SPACING = 110;
	private static final double SINGLE_SPACING = 125;
	
	private static final int STRAIN_STEP = 400;
	
	private static final float CIRCLE_SIZE_BUFF_TRESHOLD = 30;
	
	private static final byte DIFF_SPEED = 0;
	private static final byte DIFF_AIM = 1;
	
	private List<DifficultyObject> difficultyObjects;
	
	public DifficultyCalculator(Beatmap beatmap) {
		double radius = (PLAYFIELD_WIDTH/16)*(1 - 0.7*(beatmap.getDifficultySettings().getCS() - 5)/5);
		
		difficultyObjects = beatmap.getHitObjects().stream()
			.map(o->new DifficultyObject(o, radius))
			.collect(Collectors.toList());
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			if (previous != null) 
				current.calculateStrains(previous);
			previous = current;
		}
	}
	
	public Difficulty calculate() {
		double aimDifficulty = Math.sqrt(calculateDifficulty(DIFF_AIM))*STAR_SCALING_FACTOR;
		double speedDifficulty = Math.sqrt(calculateDifficulty(DIFF_SPEED))*STAR_SCALING_FACTOR;
		
		//aimDifficulty = (double)Math.round(aimDifficulty*100)/100;
		//speedDifficulty = (double)Math.round(speedDifficulty*100)/100;
		
		double starDifficulty = aimDifficulty + speedDifficulty + Math.abs(speedDifficulty - aimDifficulty)*EXTREME_SCALING_FACTOR;
		return new Difficulty(aimDifficulty, speedDifficulty, starDifficulty);
	}
	
	private double calculateDifficulty(byte difficultyType) {
		List<Double> highestStrains = new ArrayList<>();
		long intervalEnd = STRAIN_STEP;
		double maxStrain = 0;
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			while (current.object.getStartTime() > intervalEnd) {
				highestStrains.add(maxStrain);
				if (previous != null) {
					double decay = Math.pow(DECAY_BASE[difficultyType], (double)(intervalEnd - previous.object.getStartTime())/1000);
					maxStrain = previous.strains[difficultyType]*decay;
				}
				intervalEnd += STRAIN_STEP;
			}
			maxStrain = Math.max(maxStrain, current.strains[difficultyType]);
			previous = current;
		}
		
		double difficulty = 0, weight = 1;
		Collections.sort(highestStrains, (a,b)->(int)(Math.signum(b-a)));
		
		for (double strain : highestStrains) {
			difficulty += weight*strain;
			weight *= DECAY_WEIGHT;
		}
		
		return difficulty;
	}

	class DifficultyObject {
		
		private HitObject object;
		private double[] strains = {1, 1};
		private Vec2 normStart;//, normEnd;
		
		DifficultyObject(HitObject object, double radius) {
			this.object = object;
			
			double scalingFactor = 52/radius;
			if (radius < CIRCLE_SIZE_BUFF_TRESHOLD)
				scalingFactor *= 1 + Math.min(CIRCLE_SIZE_BUFF_TRESHOLD - radius, 5) / 50;
			
			normStart = object.getPosition().scale(scalingFactor);
			//normEnd = normStart;
		}
		
		private void calculateStrains(DifficultyObject previous) {
			calculateStrain(previous, DIFF_SPEED);
			calculateStrain(previous, DIFF_AIM);
		}
		
		private void calculateStrain(DifficultyObject previous, byte difficultyType) {
			double res = 0;
			long timeElapsed = object.getStartTime() - previous.object.getStartTime();
			double decay = Math.pow(DECAY_BASE[difficultyType], timeElapsed/1000f);
			double scaling = WEIGHT_SCALING[difficultyType];
			
			if (!(object instanceof Spinner)) {
				double distance = normStart.distance(previous.normStart);
				res = spacingWeight(distance, difficultyType)*scaling;
			}
			
			res /= Math.max(timeElapsed, 50);
			strains[difficultyType] = previous.strains[difficultyType]*decay + res;
		}
		
		private double spacingWeight(double distance, byte difficultyType) {
			if (difficultyType == DIFF_SPEED) {
				if (distance > SINGLE_SPACING) {
					return 2.5;
				}
				else if (distance > STREAM_SPACING){
					return 1.6 + 0.9*(distance - STREAM_SPACING)/(SINGLE_SPACING - STREAM_SPACING);
				}
				else if (distance > ALMOST_DIAMETER) {
					return 1.2 + 0.4*(distance - ALMOST_DIAMETER)/(STREAM_SPACING - ALMOST_DIAMETER);
				}
				else if (distance > ALMOST_DIAMETER/2) {
					return 0.95 + 0.25*(distance - ALMOST_DIAMETER/2)/(ALMOST_DIAMETER/2);
				}
				return 0.95;
			}
			else if (difficultyType == DIFF_AIM)
				return Math.pow(distance, 0.99);
			else
				return 0;
		}
	}
}
