package dev.azn9.beatcraft.commands;
import dev.azn9.beatcraft.Beatcraft;
import dev.azn9.beatcraft.game.GameService;
import dev.azn9.beatcraft.osu.OsuMap;
import dev.azn9.beatcraft.utils.IOUtil;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LoadCommand implements CommandExecutor {

    private final Beatcraft   plugin;
    private final GameService gameService;

    public LoadCommand(Beatcraft plugin, GameService gameService) {
        this.plugin = plugin;
        this.gameService = gameService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;

        if (args.length != 1 || !args[0].matches("https://osu.ppy.sh/beatmapsets/[0-9]{1,10}")) {
            sender.sendMessage("§c/load https://osu.ppy.sh/beatmapsets/XXXXXX");
            return true;
        }

        String url = args[0];
        String beatmapId = url.split("s/")[1];

        String downloadUrl = "https://api.chimu.moe/v1/download/" + beatmapId + "?n=1";

        sender.sendMessage("§aLoading beatmap...");

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            File zipFile = new File("map.zip");
            File mapFolder = new File("map");

            if (!mapFolder.exists())
                mapFolder.mkdir();
            else
                for (File file : mapFolder.listFiles())
                    file.delete();

            this.gameService.clearMaps();

            try {
                IOUtil.download(downloadUrl, zipFile);
                IOUtil.unzip(zipFile, mapFolder);
            } catch (IOException e) {
                sender.sendMessage("§cUne erreur est survenue !");
                e.printStackTrace();
                return;
            }

            List<File> mapFiles = new ArrayList<>();

            for (File file : mapFolder.listFiles())
                if (file.getName().contains(".osu"))
                    mapFiles.add(file);

            sender.sendMessage("§7Choisissez la difficulté à jouer :");

            for (File mapFile : mapFiles) {
                OsuMap osuMap;

                try {
                    osuMap = OsuMap.parseFile(mapFile);
                    this.gameService.addMap(osuMap);
                } catch (Exception e) {
                    e.printStackTrace();

                    sender.sendMessage("§cUne erreur est survenue lors de la lecture du fichier '" + mapFile.getName() + "' !");
                    continue;
                }

                if (osuMap == null)
                    continue;

                TextComponent textComponent = new TextComponent("§7» " + osuMap.title + " §7[" + osuMap.getColor() + osuMap.difficulty + "§7]");
                textComponent.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/play " + osuMap.mapId));
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(osuMap.getColor() + "❖ §f" + String.format("%.1f", osuMap.star))));

                sender.sendMessage(textComponent);
            }
        });

        return true;
    }
}
