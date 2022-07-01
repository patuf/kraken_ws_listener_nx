package poller;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.socket.*;
import poller.model.SubscribeMessage;
import poller.snapshot.SnapshotOperator;

/**
 * WebSocket handler that handles the meta-communication, i.e. heartbeats and subscription responses.
 * It passes through to the snapshotOperator the rest of the messages, i.e. trade notification messages.
 * No connection failed handling is implemented.
 */
public class SnapshotKeepingWebSocketHandler implements WebSocketHandler {
    protected static final Log logger = LogFactory.getLog(PollingApp.class);
    private SubscribeMessage subscribeMessage;
    private SnapshotOperator snapshotOperator;
    private Gson gson;

    public SnapshotKeepingWebSocketHandler(SubscribeMessage subscribeMessage, SnapshotOperator snapshotOperator) {
        this.subscribeMessage = subscribeMessage;
        this.snapshotOperator = snapshotOperator;
        this.gson = new Gson();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        session.sendMessage(new TextMessage(gson.toJson(subscribeMessage)));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//        System.out.println(message.getPayload()); // The logger is in a separate thread, so it can't be trusted for the order of messages on screen.
        String messagePayload = message.getPayload().toString();
        if (messagePayload.startsWith("{\"event\":\"heartbeat\"}") ||
                messagePayload.startsWith("{\"channelID\":") ||
                messagePayload.startsWith("{\"connectionID\":")) {
            // A bit of a clumsy check, but all the alternatives (i.e. parse the json or regex match) will be slower.
            return;
        }
        snapshotOperator.processTradeMessage(messagePayload);
        snapshotOperator.dumpAllSnapshots(System.out);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("Transport error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("Connection closed!");
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
