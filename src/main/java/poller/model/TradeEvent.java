package poller.model;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

/**
 * The very basic trade event model.
 * This model is enough to map the Orderbook trade events,
 * Extending classes can augment with additional fields
 */
public class TradeEvent {
    private static DecimalFormat df = new DecimalFormat("##.########");
    private double price;
    private double amount;
    private LocalDateTime ts;

    public TradeEvent(double price, double amount, LocalDateTime ts) {
        this.price = price;
        this.amount = amount;
        this.ts = ts;
    }

    public double getPrice() {
        return price;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return new StringBuilder("[")
                .append(df.format(getPrice()))
                .append(", ")
                .append(df.format(getAmount()))
                .append("]")
                .toString();
    }
}
