package lt.ekgame.beatmap_analyzer.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import lt.ekgame.beatmap_analyzer.beatmap.mania.ManiaBeatmap;
import lt.ekgame.beatmap_analyzer.difficulty.ManiaDifficulty;
import lt.ekgame.beatmap_analyzer.parser.BeatmapException;
import lt.ekgame.beatmap_analyzer.parser.BeatmapParser;
import lt.ekgame.beatmap_analyzer.performance.Performance;
import lt.ekgame.beatmap_analyzer.performance.scores.Score;
import lt.ekgame.beatmap_analyzer.utils.Mods;

public class TestMania {

	@Test
	public void test() throws FileNotFoundException, BeatmapException {
		BeatmapParser parser = new BeatmapParser();
		//File file = new File("C:\\Program Files\\osu!\\Songs\\341207  - VSRG Pattern Training\\5min (iJinjin) [test2].osu");
		File file = new File("test_maps/mania/maniera.osu");
		ManiaBeatmap beatmap = parser.parse(file, ManiaBeatmap.class);//.withMods(new Mods(Mod.DOUBLE_TIME));
		ManiaDifficulty diff = beatmap.getDifficulty();
		System.out.println("stars: " + beatmap.getDifficulty().getStars());
		
		Score score = Score.of(beatmap).score(978993).maniaAccuracy(39, 2, 1, 2).build();
		Performance perf = diff.getPerformance(score);
		System.out.println("\nacc: " + perf.getAccuracy());
		System.out.println("pp: " + perf.getPerformance());
		System.out.println("strain: " + perf.getSpeedPerformance());
		System.out.println("acc: " + perf.getAccuracyPerformance());
		
		ManiaDifficulty diffReal = new ManiaDifficulty(beatmap, Mods.NOMOD, 7.949989318847656, null);
		Performance perf2 = diffReal.getPerformance(score);
		System.out.println("\nacc: " + perf2.getAccuracy());
		System.out.println("pp: " + perf2.getPerformance());
		System.out.println("strain: " + perf2.getSpeedPerformance());
		System.out.println("acc: " + perf2.getAccuracyPerformance());
	}

}
