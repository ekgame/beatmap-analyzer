package lt.ekgame.beatmap_analyzer.test;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import lt.ekgame.beatmap_analyzer.beatmap.taiko.TaikoBeatmap;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TestTaiko {

	@Test
	public void test() throws FileNotFoundException, BeatmapException {
		String[] files = {
			"tomorrow_perfume_oni.osu",
			"tomorrow_perfume_kantan.osu",
			"tomorrow_perfume_muzukashii.osu",
			"tomorrow_perfume_futsuu.osu",
			"tomorrow_perfume_inner_oni.osu",
			"tomorrow_perfume_nardo_inner_oni.osu"
		};
		
		BeatmapParser parser = new BeatmapParser();
		System.out.println("NOMOD");
		for (String filename : files)
			testMap(parser, new File("test_maps/taiko/" + filename), null);
		
		System.out.println("\nDoubleTime");
		for (String filename : files)
			testMap(parser, new File("test_maps/taiko/" + filename), new Mods(Mod.DOUBLE_TIME));
		
		System.out.println("\nHalfTime");
		for (String filename : files)
			testMap(parser, new File("test_maps/taiko/" + filename), new Mods(Mod.HALF_TIME));
	}
	
	private void testMap(BeatmapParser parser, File file, Mods mods) throws FileNotFoundException, BeatmapException {
		TaikoBeatmap beatmap = (TaikoBeatmap) parser.parse(file);
		if (mods != null)
			beatmap = beatmap.withMods(mods);
		System.out.println(beatmap.getMetadata().getVersion() + ": " + beatmap.getDifficulty().getStarDifficulty());
	}

}
