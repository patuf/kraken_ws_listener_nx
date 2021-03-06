package poller.snapshot;

import poller.model.TradeEvent;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

/**
 * An implementation of this class would represent an in-memory snapshot of a particular channel.
 * @param <V> The very basic TradeEvent, or its descendant, if necessary.
 */
public interface AssetSnapshot<V extends TradeEvent> {

    SortedMap<Double, V> getAsks();
    void putAsk(V ask);
    SortedMap<Double, V> getBids();
    void putBid(V bid);

    String getAsset();
    V getBestAsk();
    V getBestBid();
    LocalDateTime getLatestTimestamp();
}
