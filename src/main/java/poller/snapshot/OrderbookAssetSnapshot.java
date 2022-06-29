package poller.snapshot;

import poller.model.TradeEvent;

import java.time.LocalDateTime;
import java.util.*;

/**
 * This particular implementation of AssetSnapshot keeps both asks and bids
 * sorted in descending order by their price. It is assumed that the price
 * is the unique identifier of a TradeEvent (ask or bid), and I hope I was
 * right to assume that. The API documentation doesn't explain what is the
 * unique key, if any, so I assumed that Kraken sends the total at each price,
 * thus two different elements at the same price, aren't supposed to exist.
 */
public class OrderbookAssetSnapshot implements AssetSnapshot<TradeEvent> {
    private static final String ZERO_STRING = "0.00000000";

    private TreeMap<String, TradeEvent> asks;
    private TreeMap<String, TradeEvent> bids;
    private TradeEventComparator comparator = new TradeEventComparator();
    private String asset;
    private LocalDateTime latestTimestamp;

    public OrderbookAssetSnapshot(String asset) {
        this.asset = asset;
        asks = new TreeMap<>(comparator);
        bids = new TreeMap<>(comparator);
    }

    @Override
    public SortedMap<String, TradeEvent> getAsks() {
        return asks;
    }

    @Override
    public void putAsk(TradeEvent ask) {
        if (ZERO_STRING.equals(ask.getAmount())) {
            asks.remove(ask.getPrice());
        } else {
            asks.put(ask.getPrice(), ask);
        }
        if (asks.size() > 10) {
//            asks.remove(asks.firstKey());
            asks.pollFirstEntry();
        }
        setLatestTimestamp(ask.getTs());
    }

    @Override
    public SortedMap<String, TradeEvent> getBids() {
        return bids;
    }

    @Override
    public void putBid(TradeEvent bid) {
        if (ZERO_STRING.equals(bid.getAmount())) {
            bids.remove(bid.getPrice());
        } else {
            bids.put(bid.getPrice(), bid);
        }
        if (bids.size() > 10) {
//            bids.remove(bids.lastKey());
            bids.pollLastEntry();
        }
        setLatestTimestamp(bid.getTs());
    }

    @Override
    public String getAsset() {
        return this.asset;
    }

    @Override
    public TradeEvent getBestAsk() {
        return asks.lastEntry().getValue();
    }

    @Override
    public TradeEvent getBestBid() {
        return bids.firstEntry().getValue();
    }

    @Override
    public LocalDateTime getLatestTimestamp() {
        return latestTimestamp;
    }

    protected void setLatestTimestamp(LocalDateTime latestTimestamp) {
        if (this.latestTimestamp == null || latestTimestamp.isAfter(this.latestTimestamp)) {
            this.latestTimestamp = latestTimestamp;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("<------------------------------------>\nasks:\n");
        asks.forEach((key, event) -> builder.append('\t').append(event.toString()).append("\n"));
        builder.append("best ask: ");
        if (asks.isEmpty()) {
            builder.append("none\n");
        } else {
            builder.append(getBestAsk().toString()).append("\n");
        }
        builder.append("best bid: ");
        if (bids.isEmpty()) {
            builder.append("none\n");
        } else {
            builder.append(getBestBid().toString()).append("\n");
        }

        builder.append("bids:\n");
        bids.forEach((key, event) -> builder.append('\t').append(event.toString()).append("\n"));

        if (!bids.isEmpty() || !asks.isEmpty()) {
            builder.append(getLatestTimestamp()).append("\n");
        }

        builder.append(getAsset()).append("\n")
                .append(">-------------------------------------<\n");

        return builder.toString();
    }

    private static class TradeEventComparator implements Comparator<String> {

        @Override
        public int compare(String te1, String te2) {
            return te2.compareTo(te1);
        }
    }
}
