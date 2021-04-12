package dev.azn9.beatcraft.commands;
import dev.azn9.beatcraft.Beatcraft;
import dev.azn9.beatcraft.game.GameService;
import dev.azn9.beatcraft.osu.OsuMap;
import java.io.File;
import java.io.IOException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.FileUtil;
import org.jetbrains.annotations.NotNull;

public class PlayCommand implements CommandExecutor {

    private final GameService gameService;
    private final Beatcraft   plugin;

    public PlayCommand(Beatcraft plugin, GameService gameService) {
        this.plugin = plugin;
        this.gameService = gameService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;

        if (args.length != 1)
            return true;

        OsuMap osuMap = null;
        for (OsuMap availableMap : gameService.getAvailableMaps()) {
            if (("" + availableMap.mapId).equalsIgnoreCase(args[0])) {
                osuMap = availableMap;
                break;
            }
        }

        if (osuMap == null)
            return true;

        File audioFile = new File("map", osuMap.audioFile);

        if (!audioFile.exists()) {
            sender.sendMessage("§cAucun fichier audio trouvé !");
            return true;
        }

        File targetFile = new File(plugin.getDataFolder(), audioFile.getName());
        if (targetFile.exists())
            targetFile.delete();
        try {
            targetFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileUtil.copy(audioFile, targetFile);

        this.gameService.loadMap(osuMap);

        plugin.getWebSocketService().sendAudioUpdate(audioFile.getName());

        return true;
    }
}
