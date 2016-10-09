# Beatmap Analyzer

Beatmap Analyzer is a library for parsing osu! beatmap files, calculating their difficulty and performance values. Supports standard, Taiko and Mania gamemodes.

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