package lt.ekgame.beatmap_analyzer.parser.hitobjects;

import lt.ekgame.beatmap_analyzer.beatmap.HitObject;

public interface HitObjectParser<T extends HitObject> {
	
	T parse(String line);
	
}
