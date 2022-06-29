package poller.model;

/**
 * This class is the mapping to a "subscribe" message.
 */
public class SubscribeMessage {

    private String event = "subscribe";
    private String[] pair;
    private Subscription subscription;

    public SubscribeMessage(String[] pair, String subscription, int depth) {
        this.pair = pair;
        this.subscription = new Subscription(subscription, depth);
    }

    private class Subscription {
        public Subscription(String name, int depth) {
            this.name = name;
            this.depth = depth;
        }

        private String name;
        private int depth;
    }
}
