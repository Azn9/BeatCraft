package dev.azn9.beatcraft.ws;

import dev.azn9.beatcraft.Beatcraft;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class VerticleService extends AbstractVerticle {

    private final Beatcraft plugin;

    public VerticleService(Beatcraft plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start(Future<Void> fut) {
        try {
            Router router = Router.router(vertx);

            StaticHandler staticHandler = StaticHandler.create(plugin.getDataFolder().getPath());
            router.route("/*").handler(staticHandler);

            vertx.createHttpServer()
                    .requestHandler(router::accept)
                    .listen(config().getInteger("http.port", 1234),
                            result -> {
                                if (result.succeeded())
                                    fut.complete();
                                else
                                    fut.fail(result.cause());
                            }
                    );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
