package dev.azn9.beatcraft.ws;

import dev.azn9.beatcraft.game.GameService;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class WebSocketService extends WebSocketServer {

    private static final Map<WebSocket, Integer> SOCKET_LIST = new HashMap<>();

    private final GameService gameService;

    public WebSocketService(InetSocketAddress address, GameService gameService) {
        super(address);
        this.gameService = gameService;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
        int currentId = 0;
        SOCKET_LIST.put(conn, currentId);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        SOCKET_LIST.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        if (message.equalsIgnoreCase("READY")) {
            this.gameService.start();
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        if (conn != null)
            System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress() + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("server started successfully");
    }

    public void sendAudioUpdate(String name) {
        SOCKET_LIST.keySet().forEach(webSocket -> webSocket.send("AU:" + name));
    }

    public void startAudio() {
        SOCKET_LIST.keySet().forEach(webSocket -> webSocket.send("START"));
    }
}
