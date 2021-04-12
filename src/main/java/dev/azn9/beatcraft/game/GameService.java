package dev.azn9.beatcraft.game;

import dev.azn9.beatcraft.Beatcraft;
import dev.azn9.beatcraft.osu.OsuMap;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class GameService {

    private final Beatcraft        plugin;
    private final List<OsuMap>     availableMaps = new ArrayList<>();
    private final List<ArmorStand> armorStands   = new ArrayList<>();
    private       double           speedT;

    public GameService(Beatcraft plugin) {
        this.plugin = plugin;
    }

    public void addMap(OsuMap map) {
        this.availableMaps.add(map);
    }

    public void removeMap(OsuMap map) {
        this.availableMaps.remove(map);
    }

    public List<OsuMap> getAvailableMaps() {
        return this.availableMaps;
    }

    public void clearMaps() {
        this.availableMaps.clear();
    }

    public void start() {
        AtomicInteger count = new AtomicInteger(4);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task -> {
            if (count.get() == 1) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    //onlinePlayer.teleport()
                    onlinePlayer.sendMessage("§aGo !");
                }

                this.plugin.getWebSocketService().startAudio();

                new Timer().scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (armorStands.size() == 0)
                            cancel();

                        for (ArmorStand armorStand : armorStands) {
                            armorStand.teleportAsync(armorStand.getLocation().clone().add(-speedT, 0, 0));

                            if (armorStand.getLocation().getX() < 3.0)
                                armorStand.remove();
                        }
                    }
                }, 0L, 10);

                task.cancel();
                return;
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.sendMessage("§e" + count.decrementAndGet());
                onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 5f);
            }
        }, 0L, 20L);
    }

    public void loadMap(OsuMap osuMap) {
        //x + 3.0

        double speed = osuMap.speed / 2; // blocs / s
        this.speedT = speed / 100;
        double speedPerNote = speed / 1000;

        Random random = new SecureRandom();
        World world = Bukkit.getWorld("world");

        if (world == null)
            throw new IllegalStateException();

        int lastLine = 0; // 1 = rose, 2 = jaune, 3 = verte, 4 = bleue
        for (Double time : osuMap.hitObjects) { //en ms !
            System.out.println("Placing " + time + "...");

            int line;

            do {
                line = random.nextInt(4);
            } while (line == lastLine);

            lastLine = line;

            double x = speedPerNote * time + 3.0;
            double z = line - 0.5;

            System.out.println(x + " " + z);

            Material material;
            switch (line) {
                case 0:
                    material = Material.PINK_CONCRETE;
                    break;
                case 1:
                    material = Material.LIME_CONCRETE;
                    break;
                case 2:
                    material = Material.YELLOW_CONCRETE;
                    break;
                case 3:
                    material = Material.LIGHT_BLUE_CONCRETE;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + line);
            }

            ArmorStand armorStand = world.spawn(new Location(world, x, 201, z), ArmorStand.class);
            armorStand.setInvisible(true);
            armorStand.setItem(EquipmentSlot.HEAD, new ItemStack(material));

            this.armorStands.add(armorStand);
        }

    }
}
