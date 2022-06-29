package poller.snapshot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import poller.model.TradeEvent;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This class contains the bulk of the business logic of parsing the message and building the corresponding model.
 * It contains a registry of snapshots (currently only Orderbook snapshots are implemented).
 * Any message that comes over the Web socket, that refers to an asset not present in this class' snapshotRegistry,
 * will be ignored.
 */
public class SnapshotOperator {
    Map<String, AssetSnapshot<TradeEvent>> orderBookSnapshots;

    public SnapshotOperator() {
        this.orderBookSnapshots = new LinkedHashMap<>(); // LinkedHashMaps have faster iterator traversal than regular HashMaps
    }

    public void registerAssetSnapshot (Channel channel, AssetSnapshot<TradeEvent> snapshot) {

        switch (channel) {
            case BOOK:
                orderBookSnapshots.put(snapshot.getAsset(), snapshot);
                break;
            default:
                throw new UnsupportedOperationException("Snapshot keeping is implemented only for \"book\" channel!");
        }
    }

    /**
     * This method is the entrypoint to mapping a message to the model.
     * Since only Orderbook has been implemented, there is no need for further
     * separation of concerns, so all of the logic for Orderbook parsing is here.
     * @param messagePayload
     */
    public void processTradeMessage(String messagePayload) {
        JsonArray msgArray = JsonParser.parseString(messagePayload).getAsJsonArray();
        String channelName = msgArray.get(2).getAsString();
        if (!channelName.startsWith("book")) {
            // Only book channel is implemented, so we silently skip anything else.
            return;
        }
        String assetName = msgArray.get(3).getAsString();
        AssetSnapshot<TradeEvent> as = orderBookSnapshots.get(assetName);
        if (as != null) {
            JsonObject record = msgArray.get(1).getAsJsonObject();
            if (record.has("as")) {
                processOperations(record.getAsJsonArray("as"), as::putAsk);
            }
            if (record.has("bs")) {
                processOperations(record.getAsJsonArray("bs"), as::putBid);
            }
            if (record.has("a")) {
                processOperations(record.getAsJsonArray("a"), as::putAsk);
            }
            if (record.has("b")) {
                processOperations(record.getAsJsonArray("b"), as::putBid);
            }
        }
    }

    private void processOperations(JsonArray operations, Consumer<TradeEvent> consume) {
        for (JsonElement el: operations) {
            JsonArray operation = el.getAsJsonArray();
            double price = operation.get(0).getAsDouble();
            double amount = operation.get(1).getAsDouble();
            String timestampSt = operation.get(2).getAsString();
            int delimIdx = timestampSt.indexOf('.');
            LocalDateTime ts = LocalDateTime.ofEpochSecond(
                    Long.parseLong(timestampSt.substring(0, delimIdx)),
                    Integer.parseInt(timestampSt.substring(delimIdx + 1)),
                    ZoneOffset.UTC);
            consume.accept(new TradeEvent(price, amount, ts));
        }
    }

    public void dumpAllSnapshots(PrintStream out) {
        this.orderBookSnapshots.forEach((k, v) -> out.print(v));
//        for (Map.Entry<String, AssetSnapshot> entry: this.orderBookSnapshots.entrySet()) {
//            out.print(entry.getValue());
//        }
    }

    public enum Channel {
        BOOK
    }
}
