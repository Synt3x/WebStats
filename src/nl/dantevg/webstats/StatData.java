package nl.dantevg.webstats;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class StatData {
	Map<String, Object> online;
	@SerializedName("scoreboard") Stats stats;
	
	public StatData(Map<String, Object> online, Stats stats) {
		this.online = online;
		this.stats = stats;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
	
	public static class Stats {
		Set<String> entries;
		List<String> columns;
		Map<String, Object> scores;
		
		public Stats(Set<String> entries, List<String> columns, Map<String, Object> scores) {
			this.entries = entries;
			this.columns = columns;
			this.scores = scores;
		}
		
		public Stats(Set<String> entries, Map<String, Object> scores) {
			this.entries = entries;
			this.scores = scores;
		}
		
	}
	
}
