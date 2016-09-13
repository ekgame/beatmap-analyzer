package lt.ekgame.beatmap_analyzer.difficulty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lt.ekgame.beatmap_analyzer.beatmap.taiko.*;
import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoCircle.TaikoColor;

public class TaikoDifficultyCalculator implements DifficultyCalculator {
	
	private static final double STAR_SCALING_FACTOR = 0.04125;
	private static final double DECAY_WEIGHT = 0.9;
	private static final int STRAIN_STEP = 400;
	
	private static final double DECAY_BASE = 0.30;
	private static final double COLOR_CHANGE_BONUS = 0.75;
	
	private static final double RHYTHM_CHANGE_BONUS = 1.0;
    private static final double RHYTHM_CHANGE_BASE_THRESHOLD = 0.2;
    private static final double RHYTHM_CHANGE_BASE = 2.0;
    
    private List<DifficultyObject> difficultyObjects;
    private final double timeRate;
	
	public TaikoDifficultyCalculator(TaikoBeatmap beatmap) {
		difficultyObjects = beatmap.getHitObjects().stream()
			.map(o->new DifficultyObject(o))
			.sorted((a, b)-> a.object.getStartTime() - b.object.getStartTime())
			.collect(Collectors.toList());
		
		timeRate = beatmap.getMods().getTimeRate();
		
		DifficultyObject previous = null;
		for (DifficultyObject current : difficultyObjects) {
			if (previous != null) 
				current.calculateStrain(previous);
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
					double decay = Math.pow(DECAY_BASE, (double)(intervalEnd - previous.object.getStartTime())/1000);
					maxStrain = previous.strain*decay;
				}
				intervalEnd += realStrainStep;
			}
			maxStrain = Math.max(maxStrain, current.strain);
			previous = current;
		}
		
		double difficulty = 0, weight = 1;
		Collections.sort(highestStrains, (a,b)->(int)(Math.signum(b-a)));
		
		for (double strain : highestStrains) {
			difficulty += weight*strain;
			weight *= DECAY_WEIGHT;
		}
		
		difficulty *= STAR_SCALING_FACTOR;
		
		return new Difficulty(0, 0, difficulty);
	}
	
	enum ColorSwitch {
		NONE, EVEN, ODD
	}

	class DifficultyObject {
		
		private TaikoObject object;
		private double strain = 1;
		private double timeElapsed;
		private boolean isBlue = false;
		private int sameColorChain = 0;
		private ColorSwitch lastColorSwitch = ColorSwitch.NONE;
		
		DifficultyObject(TaikoObject object) {
			this.object = object;
			
			// XXX: can drumrolls be blue?
			if (object instanceof TaikoCircle)
				isBlue = ((TaikoCircle) object).getColor() == TaikoColor.BLUE;
		}
		
		private void calculateStrain(DifficultyObject previous) {
			timeElapsed = (object.getStartTime() - previous.object.getStartTime())/timeRate;
			double decay = Math.pow(DECAY_BASE, timeElapsed/1000);
			double addition = 1;
			
			
			boolean isClose = object.getStartTime() - previous.object.getStartTime() < 1000;
			if (object instanceof TaikoCircle && previous.object instanceof TaikoCircle && isClose) {
				addition += colorChangeAddition(previous);
				addition += rhythmChangeAddition(previous);
			}
			
			double additionFactor = 1;
			if (timeElapsed < 50)
				additionFactor = 0.4 + 0.6*timeElapsed/50;
			
			strain = previous.strain*decay + addition*additionFactor;
		}
		
		private double colorChangeAddition(DifficultyObject previous) {
			if (isBlue != previous.isBlue) {
				lastColorSwitch = previous.sameColorChain % 2 == 0 ? ColorSwitch.EVEN : ColorSwitch.ODD;
				
				switch (previous.lastColorSwitch) {
				case EVEN:
					if (lastColorSwitch == ColorSwitch.ODD)
						return COLOR_CHANGE_BONUS;
					break;
					
				case ODD:
					if (lastColorSwitch == ColorSwitch.EVEN)
						return COLOR_CHANGE_BONUS;
					break;
				}
			}
			else {
				lastColorSwitch = previous.lastColorSwitch;
				sameColorChain = previous.sameColorChain + 1;
			}
			return 0;
		}
		
		private double rhythmChangeAddition(DifficultyObject previous) {
			if (timeElapsed == 0 || previous.timeElapsed == 0)
				return 0;
			
			double timeElapsedRatio = Math.max(previous.timeElapsed/timeElapsed, timeElapsed/previous.timeElapsed);
			if (timeElapsedRatio > 8)
				return 0;
			
			double difference = (Math.log(timeElapsedRatio)/Math.log(RHYTHM_CHANGE_BASE)) % 1.0;
			if (difference > RHYTHM_CHANGE_BASE_THRESHOLD && difference < 1 - RHYTHM_CHANGE_BASE_THRESHOLD)
				return RHYTHM_CHANGE_BONUS;
			
			return 0;
		}
	}
}
