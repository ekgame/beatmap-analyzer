# Beatmap Analyzer

Beatmap Analyzer is a library for parsing osu! beatmap files, calculating their difficulty and performance values. Supports standard, Taiko and Mania gamemodes. Conversion from standard to other gamemodes is not supported yet.

The project started off as a port of [oppai](https://github.com/Francesco149/oppai) by Francesco149, but has since evolved in to a powerful library in own right.

# Usage

```java
// Parse a beatmap.
BeatmapParser parser = new BeatmapParser();
Beatmap beatmap = parser.parse(new File("beatmap.osu"));

// Calculate difficulty, optionally apply mods.
Difficulty difficulty = beatmap.getDifficulty(new Mods(Mod.HARDROCK));

// Get the star difficulty.
difficulty.getStars();

// Calculate performance.
Score score = Score.of(beatmap).combo(2358).accuracy(0.9971, 1).build();
Performance performance = diff.getPerformance(score);

// Get the performance value.
performance.getPerformance();
```
You can find more usage examples in the tests directory.

# Accuracy

### osu!

The algorithm for osu! standard treats sliders as hit circles. This is much more efficient, however it does produce a small error for both star difficulty and performance. It should still be accurate enough for most applications, because the error is only about 0.02pp.

### osu!taiko

The algorithm for osu!taiko is the least tricky one and should be 100% accurate.

### osu!mania

Due to the nature of osu!mania difficulty calculation, it is very hard to accurately calculate difficulty and performance. Expect about 10pp error for the top plays.

The problem lies in the original implementation of the difficulty calculator. Before calculating the difficulty, osu! tries to sort all objects by their timestamp. Unfortunately, to sort the objects .NET uses an unstable sorting algorithm, which may change the order of objects on the same timestamp. Depending on the version of .NET, either quicksort or introsort is used - both of which end up with different results. Even now you can find beatmaps that have different star difficulty on the website and the osu! client. 

The official osu! server supposedly runs .NET 3.5 which uses quicksort, so this library tries to use that too. The difference of this algorithm implementation produces the error.
