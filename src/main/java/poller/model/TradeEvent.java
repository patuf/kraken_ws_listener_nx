package poller.model;

import java.time.LocalDateTime;

/**
 * The very basic trade event model.
 * This model is enough to map the Orderbook trade events,
 * Extending classes can augment with additional fields
 */
public class TradeEvent {
    private double price;
    private String amount;
    private LocalDateTime ts;

    public TradeEvent(double price, String amount, LocalDateTime ts) {
        this.price = price;
        this.amount = amount;
        this.ts = ts;
    }

    public double getPrice() {
        return price;
    }

    public String getAmount() {
        return amount;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return new StringBuilder("[")
                .append(getPrice())
                .append(", ")
                .append(getAmount())
                .append("]")
                .toString();
    }
}
