package lt.ekgame.beatmap_analyzer.difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuObject;
import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuSpinner;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import lt.ekgame.beatmap_analyzer.utils.Vec2;

public class OsuDifficultyCalculator implements DifficultyCalculator {

	public static final double DECAY_BASE[] = {0.3, 0.15};
	public static final double WEIGHT_SCALING[] = {1400, 26.25};
	public static final double STAR_SCALING_FACTOR = 0.0675;
	public static final double EXTREME_SCALING_FACTOR = 0.5;
	public static final float PLAYFIELD_WIDTH = 512;
	public static final double DECAY_WEIGHT = 0.9;
	
	public static final double ALMOST_DIAMETER = 90;
	public static final double STREAM_SPACING = 110;
	public static final double SINGLE_SPACING = 125;
	
	public static final int STRAIN_STEP = 400;
	
	public static final float CIRCLE_SIZE_BUFF_TRESHOLD = 30;
	
	public static final byte DIFF_SPEED = 0;
	public static final byte DIFF_AIM = 1;
	
	@Override
	public OsuDifficulty calculate(Mods mods, Beatmap beatmap) {
		OsuBeatmap bm = (OsuBeatmap) beatmap;
		
		double timeRate = mods.getSpeedMultiplier();
		List<OsuObject> hitObjects = bm.getHitObjects();
		List<DifficultyObject> objects = generateDifficultyObjects(hitObjects, bm.getCS(mods), timeRate);
		
		List<Double> aimStrains = calculateStrains(objects, DIFF_AIM, timeRate);
		List<Double> speedStrains = calculateStrains(objects, DIFF_SPEED, timeRate);
		
		List<Double> aimStrainsOriginal = new ArrayList<>(aimStrains);
		List<Double> speedStrainsOriginal = new ArrayList<>(speedStrains);
		
		double aimDifficulty = calculateDifficulty(aimStrains);
		double speedDifficulty = calculateDifficulty(speedStrains);
		
		double starDifficulty = aimDifficulty + speedDifficulty + Math.abs(speedDifficulty - aimDifficulty)*EXTREME_SCALING_FACTOR;
		return new OsuDifficulty(bm, mods, starDifficulty, aimDifficulty, speedDifficulty, aimStrainsOriginal, speedStrainsOriginal);
	}
	
	private List<DifficultyObject> generateDifficultyObjects(List<OsuObject> hitObjects, double csRating, double timeRate) {
		double radius = (PLAYFIELD_WIDTH/16)*(1 - 0.7*(csRating - 5)/5);
		
		List<DifficultyObject> difficultyObjects = hitObjects.stream()
			.map(o->new DifficultyObject(o, radius))
			.sorted((a, b)-> a.object.getStartTime() - b.object.getStartTime())
			.collect(Collectors.toList());
    	
    	DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			if (previous != null)
				current.calculateStrains(previous, timeRate);
			previous = current;
		}
		
		return difficultyObjects;
    }
	
	private List<Double> calculateStrains(List<DifficultyObject> objects, byte difficultyType, double timeRate) {
		List<Double> highestStrains = new ArrayList<>();
		double realStrainStep = STRAIN_STEP*timeRate;
		double intervalEnd = realStrainStep;
		double maxStrain = 0;
		
		DifficultyObject previous = null;
		for (DifficultyObject current : objects) {
			while (current.object.getStartTime() > intervalEnd) {
				highestStrains.add(maxStrain);
				if (previous != null) {
					double decay = Math.pow(
						DECAY_BASE[difficultyType],
						(double)(intervalEnd - previous.object.getStartTime())/1000
					);
					maxStrain = previous.strains[difficultyType]*decay;
				}
				intervalEnd += STRAIN_STEP;
			}
			maxStrain = Math.max(maxStrain, current.strains[difficultyType]);
			previous = current;
		}
		return highestStrains;
	}
	
	public double calculateDifficulty(List<Double> strains) {
		Collections.sort(strains, (a,b)->(int)(Math.signum(b-a)));
		
		double difficulty = 0, weight = 1;
		for (double strain : strains) {
			difficulty += weight*strain;
			weight *= DECAY_WEIGHT;
		}
		
		return Math.sqrt(difficulty)*STAR_SCALING_FACTOR;
	}

	class DifficultyObject {
		
		private OsuObject object;
		private double[] strains = {1, 1};
		private Vec2 normStart;//, normEnd;
		
		DifficultyObject(OsuObject object, double radius) {
			this.object = object;
			
			double scalingFactor = 52/radius;
			if (radius < CIRCLE_SIZE_BUFF_TRESHOLD)
				scalingFactor *= 1 + Math.min(CIRCLE_SIZE_BUFF_TRESHOLD - radius, 5) / 50;
			
			normStart = object.getPosition().scale(scalingFactor);
			//normEnd = normStart;
		}
		
		private void calculateStrains(DifficultyObject previous, double timeRate) {
			calculateStrain(previous, timeRate, DIFF_SPEED);
			calculateStrain(previous, timeRate, DIFF_AIM);
		}
		
		private void calculateStrain(DifficultyObject previous, double timeRate, byte difficultyType) {
			double res = 0;
			double timeElapsed = (object.getStartTime() - previous.object.getStartTime())/timeRate;
			double decay = Math.pow(DECAY_BASE[difficultyType], timeElapsed/1000f);
			double scaling = WEIGHT_SCALING[difficultyType];
			
			if (!(object instanceof OsuSpinner)) {
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
