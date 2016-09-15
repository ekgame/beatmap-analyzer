package lt.ekgame.beatmap_analyzer.difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaObject;
import lt.ekgame.beatmap_analyzer.utils.MathUtils;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import lt.ekgame.beatmap_analyzer.utils.Quicksort;

public class ManiaDifficultyCalculator implements DifficultyCalculator<ManiaBeatmap> {
	
	private static final double STAR_SCALING_FACTOR = 0.018;
	
	private static final double INDIVIDUAL_DECAY_BASE = 0.125;
	private static final double OVERALL_DECAY_BASE = 0.3;
	
	private static final double DECAY_WEIGHT = 0.9;
	private static final int STRAIN_STEP = 400;
	
	public Difficulty<ManiaBeatmap> calculate(Mods mods, ManiaBeatmap beatmap) {
		double timeRate = mods.getSpeedMultiplier();
		List<ManiaObject> hitObjects = beatmap.getHitObjects();
		List<DifficultyObject> objects = generateDifficultyObjects(hitObjects, timeRate, beatmap.getCollumns());
		List<Double> strains = calculateStrains(objects, timeRate);
		double difficulty = calculateDifficulty(strains);
		return new Difficulty<ManiaBeatmap>(beatmap, mods, difficulty);
	}
	
	private List<DifficultyObject> generateDifficultyObjects(List<ManiaObject> hitObjects, double timeRate, int collumns) {
    	List<DifficultyObject> difficultyObjects = hitObjects.stream()
			.map(o->new DifficultyObject(o, collumns))
			.collect(Collectors.toList());
		
		// .NET 4.0 framework uses quicksort - an unstable algorithm, which means
		// that objects with equal value may change order. 
    	// Mania has lots of notes on the same timestamp.
    	// This algorithm depends on the order of the difficulty objects.
		// This is still not precise and might produce about 0.05 error for the final result.
		Quicksort.sort(difficultyObjects);
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			if (previous != null) 
				current.calculateStrains(previous, timeRate, collumns);
			previous = current;
		}
		
		return difficultyObjects;
    }
	
	private List<Double> calculateStrains(List<DifficultyObject> difficultyObjects, double timeRate) {
		List<Double> highestStrains = new ArrayList<>();
		double realStrainStep = STRAIN_STEP*timeRate;
		double intervalEnd = realStrainStep;
		double maxStrain = 0;
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			while (current.object.getStartTime() > intervalEnd) {
				highestStrains.add(maxStrain);
				if (previous != null) {
					double individualDecay = Math.pow(INDIVIDUAL_DECAY_BASE, (intervalEnd - previous.object.getStartTime())/1000.0);
					double overallDecay = Math.pow(OVERALL_DECAY_BASE, (intervalEnd - previous.object.getStartTime())/1000.0);
					maxStrain = previous.individualStrains[previous.collumn]*individualDecay + previous.strain*overallDecay;
				}
				intervalEnd += realStrainStep;
			}
			maxStrain = Math.max(maxStrain, current.individualStrains[current.collumn] + current.strain);
			previous = current;
		}
		
		Collections.sort(highestStrains, (a,b)->b.compareTo(a));
		return highestStrains;
	}
	
	private double calculateDifficulty(List<Double> strains) {
		double difficulty = 0, weight = 1;
		for (double strain : strains) {
			difficulty += weight*strain;
			weight *= DECAY_WEIGHT;
		}
		return difficulty*STAR_SCALING_FACTOR;
	}
	
	class DifficultyObject implements Comparable<DifficultyObject> {
		
		@Override
		public int compareTo(DifficultyObject o) {
			return object.getStartTime() - o.object.getStartTime();
		}
		
		private final ManiaObject object;
		private final int collumn;
		
		private double strain = 1;
		private double[] individualStrains;
		private double[] heldUntil;
		
		DifficultyObject(ManiaObject object, int collumns) {
			this.object = object;
			
			individualStrains = new double[collumns];
			heldUntil = new double[collumns];
			collumn = MathUtils.calculateManiaCollumn(object.getPosition().getX(), collumns);
		}
		
		private void calculateStrains(DifficultyObject previous, double timeRate, int collumns) {
			double timeElapsed = (object.getStartTime() - previous.object.getStartTime())/timeRate;
			double individualDecay = Math.pow(INDIVIDUAL_DECAY_BASE, timeElapsed/1000);
			double overallDecay = Math.pow(OVERALL_DECAY_BASE, timeElapsed/1000);
			double holdFactor = 1;
			double holdAddition = 0;
			
			for (int i = 0; i < collumns; i++) {
				heldUntil[i] = previous.heldUntil[i];
				
				if (object.getStartTime() < heldUntil[i] && object.getEndTime() > heldUntil[i] && object.getEndTime() != heldUntil[i])
					holdAddition = 1;

                if (heldUntil[i] > object.getEndTime())
                	holdFactor = 1.25;
                
                individualStrains[i] = previous.individualStrains[i]*individualDecay;
			}
			
			heldUntil[collumn] = object.getEndTime();
			individualStrains[collumn] += 2*holdFactor;
			strain = previous.strain*overallDecay + (1 + holdAddition)*holdFactor;
		}
	}
}
