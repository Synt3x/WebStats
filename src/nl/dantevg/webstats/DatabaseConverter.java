package nl.dantevg.webstats;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseConverter {
	private final DatabaseConnection conn;
	private final String table;
	private final List<List<String>> conversions;
	
	public DatabaseConverter(DatabaseConnection conn, String table, List<List<String>> conversions) {
		this.conn = conn;
		this.table = table;
		this.conversions = conversions;
	}
	
	public List<Map<String, String>> getValues() {
		List<Map<String, String>> data = conn.getTable(table);
		for (List<String> command : conversions) {
			switch (command.get(0)) {
				case "filter" -> filter(data, command);
				case "remove" -> remove(data, command);
				case "rename" -> rename(data, command);
				case "key-value" -> key_value(data, command);
				case "json" -> json(data, command);
				case "uuid" -> uuid(data, command);
				default -> Main.logger.log(Level.WARNING, "Invalid conversion command '" + command.get(0) + "'");
			}
		}
		return data;
	}
	
	// [filter, column...]
	private static void filter(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			// key does not appear in argument list (or key is "filter")
			row.entrySet().removeIf(entry -> command.indexOf(entry.getKey()) <= 0);
		}
	}
	
	// [remove, column...]
	private static void remove(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			// key does appear in argument list (or key is "filter")
			row.entrySet().removeIf(entry -> command.indexOf(entry.getKey()) > 0);
		}
	}
	
	// [rename, from, to]
	private static void rename(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			String from = command.get(1), to = command.get(2);
			row.put(to, row.get(from));
			row.remove(from);
		}
	}
	
	// [key-value, key-column, value-column]
	private static void key_value(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			String key = command.get(1), value = command.get(2);
			row.put(row.get(key), row.get(value));
			row.remove(key);
			row.remove(value);
		}
	}
	
	// [json, column]
	private static void json(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			String column = command.get(1);
			JSONParser parser = new JSONParser();
			try {
				row.putAll((JSONObject) parser.parse(row.get(column)));
			} catch (ParseException e) {
				Main.logger.log(Level.WARNING, "could not decode json data", e);
			}
			
			row.remove(column);
		}
	}
	
	// [uuid, column]
	private static void uuid(List<Map<String, String>> data, List<String> command) {
		for (Map<String, String> row : data) {
			String column = command.get(1);
			UUID uuid = UUID.fromString(row.get(column));
			row.remove(column);
			String name = Bukkit.getOfflinePlayer(uuid).getName();
			if (name == null) name = getPlayerNameOnline(uuid);
			if (name != null) {
				row.put("player", name);
			} else {
				Main.logger.log(Level.WARNING, "unable to get player name for UUID " + uuid.toString());
			}
		}
	}
	
	private static String getPlayerNameOnline(UUID uuid) {
		try {
			URL url = new URL("https://api.mojang.com/user/profiles/" + uuid.toString() + "/names");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			JSONParser parser = new JSONParser();
			// Data comes in from mojang API so casting *should* be fine?
			// Structure: array of objects containing fields "name" and optionally "changedToAt"
			JSONArray names = (JSONArray) parser.parse(new InputStreamReader(conn.getInputStream()));
			return (String) ((JSONObject) names.get(names.size() - 1)).get("name");
		} catch (IOException | ParseException e) {
			Main.logger.log(Level.WARNING, "unable to retrieve player name from Mojang API", e);
		}
		return null;
	}
	
}
