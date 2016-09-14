package lt.ekgame.beatmap_analyzer.difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaObject;
import lt.ekgame.beatmap_analyzer.utils.Quicksort;

public class ManiaDifficultyCalculator implements DifficultyCalculator {
	
	private static final double STAR_SCALING_FACTOR = 0.018;
	
	private static final double INDIVIDUAL_DECAY_BASE = 0.125;
	private static final double OVERALL_DECAY_BASE = 0.3;
	
	private static final double DECAY_WEIGHT = 0.9;
	private static final int STRAIN_STEP = 400;
	
	private List<DifficultyObject> difficultyObjects;
    private final double timeRate;
	
	public ManiaDifficultyCalculator(ManiaBeatmap beatmap) {
		int numKeys = beatmap.getCollumns();
		
		difficultyObjects = beatmap.getHitObjects().stream()
			.map(o->new DifficultyObject(o, numKeys))
			//.sorted((a, b)-> a.object.getStartTime() - b.object.getStartTime())
			.collect(Collectors.toList());
		
		// This algorithm depends on the order of the difficulty objects.
		// .NET 4.0 framework uses quicksort - an unstable algorithm which means
		// that objects on the same timestamp may change order and mania has lots of those.
		// This is still not precise and might produce about 0.05 error for the final result.
		Quicksort.sort(difficultyObjects);
		
		timeRate = beatmap.getMods().getTimeRate();
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			if (previous != null) 
				current.calculateStrains(previous, numKeys);
			previous = current;
		}
	}

	@Override
	public Difficulty calculate() {
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
					maxStrain = previous.individualStrains[previous.object.getCollumn()]*individualDecay + previous.strain*overallDecay;
				}
				intervalEnd += realStrainStep;
			}
			maxStrain = Math.max(maxStrain, current.individualStrains[current.object.getCollumn()] + current.strain);
			previous = current;
		}
		
		double difficulty = 0, weight = 1;
		Collections.sort(highestStrains, (a,b)->b.compareTo(a));
		
		for (double strain : highestStrains) {
			difficulty += weight*strain;
			weight *= DECAY_WEIGHT;
		}
		
		difficulty *= STAR_SCALING_FACTOR;
		
		return new Difficulty(0, 0, difficulty);
	}
	
	class DifficultyObject implements Comparable<DifficultyObject> {
		
		@Override
		public int compareTo(DifficultyObject o) {
			return object.getStartTime() - o.object.getStartTime();
		}
		
		private ManiaObject object;
		
		private double strain = 1;
		private double[] individualStrains;
		private double[] heldUntil;
		
		DifficultyObject(ManiaObject object, int numKeys) {
			this.object = object;
			
			individualStrains = new double[numKeys];
			heldUntil = new double[numKeys];
		}
		
		private void calculateStrains(DifficultyObject previous, int numKeys) {
			double timeElapsed = (object.getStartTime() - previous.object.getStartTime())/timeRate;
			double individualDecay = Math.pow(INDIVIDUAL_DECAY_BASE, timeElapsed/1000);
			double overallDecay = Math.pow(OVERALL_DECAY_BASE, timeElapsed/1000);
			double holdFactor = 1;
			double holdAddition = 0;
			
			for (int i = 0; i < numKeys; i++) {
				heldUntil[i] = previous.heldUntil[i];
				
				if (object.getStartTime() < heldUntil[i] && object.getEndTime() > heldUntil[i] && object.getEndTime() != heldUntil[i])
					holdAddition = 1;

                if (heldUntil[i] > object.getEndTime())
                	holdFactor = 1.25;
                
                individualStrains[i] = previous.individualStrains[i]*individualDecay;
			}
			
			heldUntil[object.getCollumn()] = object.getEndTime();
			individualStrains[object.getCollumn()] += 2*holdFactor;
			strain = previous.strain*overallDecay + (1 + holdAddition)*holdFactor;
		}
	}
}
