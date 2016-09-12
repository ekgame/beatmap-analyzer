package lt.ekgame.beatmap_analyzer.test;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import lt.ekgame.beatmap_analyzer.beatmap.Beatmap;
import lt.ekgame.beatmap_analyzer.calculator.Difficulty;
import lt.ekgame.beatmap_analyzer.calculator.Performance;
import lt.ekgame.beatmap_analyzer.calculator.PerformanceCalculator;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import lt.ekgame.beatmap_analyzer.utils.Mod;
import lt.ekgame.beatmap_analyzer.utils.Mods;
import lt.ekgame.beatmap_analyzer.utils.ScoreVersion;

public class TestBeatmap {

	@Test
	public void test() throws FileNotFoundException, BeatmapException {
		BeatmapParser parser = new BeatmapParser();
		Beatmap beatmap = parser.parse(new File("blue_zenith.osu")).withMods(new Mods(Mod.HARDROCK));
		System.out.println(beatmap.getMaxCombo());
		
		Difficulty diff = beatmap.getDifficulty();
		System.out.println("stars: " + diff.getStarDifficulty());
		System.out.println("aim:   " + diff.getAimDifficulty());
		System.out.println("speed: " + diff.getSpeedDifficulty());
		
		Performance perf = beatmap.getPerformance(2358, 0.9971, 1);
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
