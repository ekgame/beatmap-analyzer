package lt.ekgame.beatmap_analyzer.utils;

public enum Mod {
	
	NO_FAIL(0, "No Fail", "nf"),
	EASY(1, "Easy", "ez"),
	HIDDEN(3, "Hidden", "hd"), 
	HARDROCK(4, "Hardrock", "hr"),
	SUDDEN_DEATH(5, "Sudden Death", "sd"),
	DOUBLE_TIME(6, "Double Time", "dt"), 
	RELAX(7, "Relax", "rx"), 
	HALF_TIME(8, "Half Time", "ht"), 
	NIGHTCORE(9, "Nightcore", "nc"), 
	FLASHLIGHT(10, "Flashlight", "fl"), 
	AUTOPLAY(11, "Autoplay", "ap"), 
	SPUN_OUT(12, "Spun Out", "so"),	
	AUTOPILOT(13, "Autopilot", "ap");
	
	private int offset;
	private String name, shortName;
	
	Mod(int offset, String name, String shortName) {
		this.offset = offset;
		this.name = name;
		this.shortName = shortName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public int getBitOffset() {
		return offset;
	}
	
	public int getBit() {
		return 1 << offset;
	}
	
	public static Mod parse(String shortName) {
		if (shortName == null)
			return null;
		
		shortName = shortName.toLowerCase();
		for (Mod mod : Mod.values())
			if (mod.equals(shortName))
				return mod;
		
		return null;
	}
}
