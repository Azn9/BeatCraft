package dev.azn9.beatcraft.osu;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.ChatColor;

public class OsuMap {

    public  long       mapId;
    public  String     audioFile;
    public  String     title;
    public  String     artist;
    public String     difficulty;
    public double       speed;
    public List<Double> hitObjects = new ArrayList<Double>();
    public double       star;

    public static OsuMap parseFile(File file) {
        OsuMap osuMap = new OsuMap();
        AtomicInteger state = new AtomicInteger();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            bufferedReader.lines().forEachOrdered(s -> {
                if (s.startsWith("[")) {
                    state.getAndIncrement();
                    return;
                }

                switch (state.get()) {
                    case 1: {
                        if (s.startsWith("AudioFilename"))
                            osuMap.audioFile = s.split(":")[1].trim();
                    }
                    break;

                    case 3: {
                        if (s.startsWith("Title:"))
                            osuMap.title = s.split(":")[1].trim();
                        if (s.startsWith("Artist:"))
                            osuMap.artist = s.split(":")[1].trim();
                        if (s.startsWith("Version"))
                            osuMap.difficulty = s.split(":")[1].trim();
                        if (s.startsWith("BeatmapID"))
                            osuMap.mapId = Long.parseLong(s.split(":")[1].trim());
                    }
                    break;

                    case 4: {
                        if (s.startsWith("ApproachRate"))
                            osuMap.speed = Double.parseDouble(s.split(":")[1].trim());
                    }
                    break;

                    case 8: {
                        if (!s.contains(",") || s.split(",").length < 3)
                            break;

                        osuMap.hitObjects.add(Double.parseDouble(s.split(",")[2]));
                    }
                    break;

                    default:
                        break;
                }
            });

            //2025942

            URLConnection connection = new URL("https://api.chimu.moe/v1/map/" + osuMap.mapId).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String jsonText = readAll(rd);

            osuMap.star = Double.parseDouble(jsonText.split("DifficultyRating\":")[1].split(",")[0].trim());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return osuMap;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public ChatColor getColor() {
        if (this.star < 2)
            return ChatColor.AQUA;
        else if (this.star < 2.7)
            return ChatColor.GREEN;
        else if (this.star < 4)
            return ChatColor.YELLOW;
        else if (this.star < 5.3)
            return ChatColor.LIGHT_PURPLE;
        else if (this.star < 6.5)
            return ChatColor.DARK_PURPLE;
        else
            return ChatColor.DARK_GRAY;
    }
}
