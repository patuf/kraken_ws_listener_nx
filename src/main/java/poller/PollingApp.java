package poller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import poller.model.SubscribeMessage;
import poller.snapshot.OrderbookAssetSnapshot;
import poller.snapshot.SnapshotOperator;

import java.net.URI;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class PollingApp {
    protected static final Log logger = LogFactory.getLog(PollingApp.class);

    public static void main(String[] args) {
        WebSocketClient client = new StandardWebSocketClient();
        SubscribeMessage subMsg = new SubscribeMessage(new String[]{
                "ETH/USD",
                "XBT/USD" // This is, as it turns out, the widely used synonym for "BTC/USD"
        }, "book", 10);
        SnapshotOperator snapshotOperator = new SnapshotOperator();
        snapshotOperator.registerAssetSnapshot(SnapshotOperator.Channel.BOOK, new OrderbookAssetSnapshot("ETH/USD"));
        snapshotOperator.registerAssetSnapshot(SnapshotOperator.Channel.BOOK, new OrderbookAssetSnapshot("XBT/USD"));
        WebSocketHandler wsHandler = new SnapshotKeepingWebSocketHandler(subMsg, snapshotOperator);

        logger.info("Attempting to open a Kraken session");
        ListenableFuture<WebSocketSession> handshake = client.doHandshake(wsHandler, null, URI.create("wss://ws.kraken.com"));
        new Scanner(System.in).nextLine(); // Awaiting for enter to stop the execution

        try {
            logger.info("Attempting to close the session");
            handshake.get(100, TimeUnit.MILLISECONDS).close();
        } catch (Exception e) {
            // Just silently exit.
        }
    }
}
