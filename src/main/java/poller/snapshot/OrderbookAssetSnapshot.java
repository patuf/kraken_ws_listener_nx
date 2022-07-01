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

    private final static String LINE_SEP = System.lineSeparator();
    private final TreeMap<Double, TradeEvent> asks;
    private final TreeMap<Double, TradeEvent> bids;
    private final String asset;
    private LocalDateTime latestTimestamp;

    public OrderbookAssetSnapshot(String asset) {
        this.asset = asset;
        TradeEventComparator comparator = new TradeEventComparator();
        asks = new TreeMap<>(comparator);
        bids = new TreeMap<>(comparator);
    }

    @Override
    public SortedMap<Double, TradeEvent> getAsks() {
        return asks;
    }

    @Override
    public void putAsk(TradeEvent ask) {
        if (ask.getAmount() == 0) {
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
    public SortedMap<Double, TradeEvent> getBids() {
        return bids;
    }

    @Override
    public void putBid(TradeEvent bid) {
        if (bid.getAmount() == 0) {
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
        StringBuilder builder = new StringBuilder("<------------------------------------>")
                .append(LINE_SEP)
                .append("asks:")
                .append(LINE_SEP);
        asks.forEach((key, event) -> builder.append('\t').append(event.toString()).append(LINE_SEP));
        builder.append("best ask: ");
        if (asks.isEmpty()) {
            builder.append("none").append(LINE_SEP);
        } else {
            builder.append(getBestAsk().toString()).append(LINE_SEP);
        }
        builder.append("best bid: ");
        if (bids.isEmpty()) {
            builder.append("none").append(LINE_SEP);
        } else {
            builder.append(getBestBid().toString()).append(LINE_SEP);
        }

        builder.append("bids:").append(LINE_SEP);
        bids.forEach((key, event) -> builder.append('\t').append(event.toString()).append(LINE_SEP));

        if (!bids.isEmpty() || !asks.isEmpty()) {
            builder.append(getLatestTimestamp()).append(LINE_SEP);
        }

        builder.append(getAsset())
                .append(LINE_SEP)
                .append(">-------------------------------------<")
                .append(LINE_SEP);

        return builder.toString();
    }

    private static class TradeEventComparator implements Comparator<Double> {

        @Override
        public int compare(Double te1, Double te2) {
            return te2.compareTo(te1);
        }
    }
}
