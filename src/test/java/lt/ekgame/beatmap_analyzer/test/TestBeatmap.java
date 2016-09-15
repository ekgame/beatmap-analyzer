package lt.ekgame.beatmap_analyzer.test;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import lt.ekgame.beatmap_analyzer.beatmap.osu.OsuBeatmap;
import lt.ekgame.beatmap_analyzer.difficulty.OsuDifficulty;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import lt.ekgame.beatmap_analyzer.performance.Performance;
import lt.ekgame.beatmap_analyzer.performance.scores.OsuScore;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TestBeatmap {

	@Test
	public void test() throws FileNotFoundException, BeatmapException {
		BeatmapParser parser = new BeatmapParser();
		OsuBeatmap beatmap = parser.parse(new File("test_maps/osu/blue_zenith.osu"), OsuBeatmap.class);
		System.out.println(beatmap.getMaxCombo());
		
		OsuDifficulty diff = beatmap.getDifficulty(new Mods(Mod.HARDROCK));
		System.out.println("stars: " + diff.getStars());
		System.out.println("aim:   " + diff.getAim());
		System.out.println("speed: " + diff.getSpeed());
		
		OsuScore score = OsuScore.of(beatmap).combo(2358).accuracy(0.9971, 1).build();
		
		Performance perf = beatmap.getPerformance(score, Mod.HARDROCK);
		System.out.println("\nacc:      " + perf.getAccuracy());
		System.out.println("aim_pp:   " + perf.getAimPerformance());
		System.out.println("speed_pp: " + perf.getSpeedPerformance());
		System.out.println("acc_pp:   " + perf.getAccuracyPerformance());
		System.out.println("total_pp: " + perf.getPerformance());
	}
	
	//@Test
	public void test_large() throws FileNotFoundException, BeatmapException {
		File folder = new File("Z:\\osu!\\Songs");
		BeatmapParser parser = new BeatmapParser();
		
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				for (File beatmapFile : file.listFiles(o->o.getAbsolutePath().toLowerCase().endsWith(".osu"))) {
					System.out.println(beatmapFile.getName());
					parser.parse(beatmapFile);
				}
			}
		}
		
		//BeatmapParser parser = new BeatmapParser();
		//Beatmap beatmap = parser.parse(new File("beatmap.osu"));
	}
}
