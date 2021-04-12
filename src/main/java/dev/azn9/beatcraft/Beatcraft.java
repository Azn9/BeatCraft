package dev.azn9.beatcraft;

import dev.azn9.beatcraft.commands.LoadCommand;
import dev.azn9.beatcraft.commands.PlayCommand;
import dev.azn9.beatcraft.game.GameService;
import dev.azn9.beatcraft.ws.VerticleService;
import dev.azn9.beatcraft.ws.WebSocketService;
import io.vertx.core.Vertx;
import java.net.InetSocketAddress;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class Beatcraft extends JavaPlugin {

    private WebSocketService WebSocketService;
    private VerticleService  verticleService;
    private GameService      gameService;

    @Override
    public void onEnable() {
        this.gameService = new GameService(this);

        this.WebSocketService = new WebSocketService(new InetSocketAddress("localhost", 8887), gameService);
        this.WebSocketService.start();

        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(this.verticleService = new VerticleService(this));

        Objects.requireNonNull(getCommand("load")).setExecutor(new LoadCommand(this, this.gameService));
        Objects.requireNonNull(getCommand("play")).setExecutor(new PlayCommand(this, this.gameService));
    }

    @Override
    public void onDisable() {
        try {
            this.WebSocketService.stop();
            this.verticleService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public WebSocketService getWebSocketService() {
        return this.WebSocketService;
    }

    public VerticleService getVerticleService() {
        return this.verticleService;
    }
}
