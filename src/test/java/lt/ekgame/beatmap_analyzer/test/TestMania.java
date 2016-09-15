package lt.ekgame.beatmap_analyzer.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;

public class TestMania {

	@Test
	public void test() throws FileNotFoundException, BeatmapException {
		BeatmapParser parser = new BeatmapParser();
		//File file = new File("C:\\Program Files\\osu!\\Songs\\341207  - VSRG Pattern Training\\5min (iJinjin) [test2].osu");
		File file = new File("test_maps/mania/putins_boner.osu");
		ManiaBeatmap beatmap = parser.parse(file, ManiaBeatmap.class);//.withMods(new Mods(Mod.DOUBLE_TIME));
		System.out.println("stars: " + beatmap.getDifficulty().getStars());
	}

}
