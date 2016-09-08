package lt.ekgame.beatmap_analyzer.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Mods {
	
	public static final Mods NOMOD = new Mods();
	
	private final int speedChangingFlags = Mod.DOUBLE_TIME.getBit() | Mod.NIGHTCORE.getBit() | Mod.HALF_TIME.getBit();
	private final int mapChangingFlags = speedChangingFlags | Mod.EASY.getBit() | Mod.HARDROCK.getBit();
	
	private List<Mod> mods = new ArrayList<>();
	private int modFlags;
	
	public Mods(List<Mod> mods) {
		for (Mod mod : mods)
			if (!this.mods.contains(mod))
				this.mods.add(mod);
		calculateFlags();
	}
	
	public Mods(Mod... mods) {
		this(Arrays.asList(mods));
	}
	
	public static Mods parse(int flags) {
		List<Mod> result = new ArrayList<>();
		for (Mod mod : Mod.values())
			if ((flags & mod.getBit()) > 0)
				result.add(mod);
		return new Mods(result);
	}
	
	public static Mods parse(String mods) {
		List<Mod> result = new ArrayList<Mod>();
        int length = mods.length();
        for (int i = 0; i < length; i += 2) {
        	String modString = mods.substring(i, Math.min(length, i + 2));
        	if (modString.length() != 2)
        		continue;
        	
        	Mod mod = Mod.parse(modString);
        	if (mod != null)
        		result.add(mod);
        }
        return new Mods(result);
	}
	
	public boolean isRanked() {
		return !mods.stream()
			.filter(mod->!mod.isRanked())
			.findAny().isPresent();
	}
	
	public Mods withoutUnranked() {
		return new Mods(mods.stream().filter(mod->mod.isRanked()).collect(Collectors.toList()));
	}
	
	public List<Mod> getMods() {
		return Collections.unmodifiableList(mods);
	}
	
	public String toString() {
		return mods.stream().map(o->o.getShortName())
			.collect(Collectors.joining()).toUpperCase();
	}
	
	public boolean isNomod() {
		return mods.isEmpty();
	}
	
	private void calculateFlags() {
		for (Mod mod : mods)
			modFlags |= mod.getBit();
	}
	
	public int getFlags() {
		return modFlags;
	}
	
	public boolean has(Mod mod) {
		return (mod.getBit() & modFlags) > 0;
	}
	
	public boolean isMapChanging() {
		return (mapChangingFlags & modFlags) > 0;
	}
	
	public boolean isSpeedChanging() {
		return (speedChangingFlags & modFlags) > 0;
	}
}
