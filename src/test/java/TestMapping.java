import org.junit.jupiter.api.*;
import poller.snapshot.OrderbookAssetSnapshot;
import poller.snapshot.SnapshotOperator;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(PER_CLASS)
public class TestMapping {
    private SnapshotOperator snapshotOperator;
    private OrderbookAssetSnapshot snapshotBtcUsd;
    private OrderbookAssetSnapshot snapshotEthUsd;

    @BeforeAll
    public void setup() {
        snapshotOperator = new SnapshotOperator();
        snapshotBtcUsd = new OrderbookAssetSnapshot("XBT/USD");
        snapshotEthUsd = new OrderbookAssetSnapshot("ETH/USD");
        snapshotOperator.registerAssetSnapshot(SnapshotOperator.Channel.BOOK, snapshotEthUsd);
        snapshotOperator.registerAssetSnapshot(SnapshotOperator.Channel.BOOK, snapshotBtcUsd);
    }

    @Test
    @Order(1)
    public void testCreateXbtUsdSnapshot() {

        String testJsonStr = "[336,{\"as\":[[\"20280.10000\",\"0.58500000\",\"1656458264.433070\"],[\"20281.20000\",\"0.12902235\",\"1656458264.034066\"],[\"20281.40000\",\"0.04854597\",\"1656458263.871885\"],[\"20281.50000\",\"0.06505876\",\"1656458263.826568\"],[\"20281.60000\",\"0.10583909\",\"1656458264.440192\"],[\"20282.10000\",\"0.20000000\",\"1656458261.313452\"],[\"20282.40000\",\"0.09113248\",\"1656458262.586700\"],[\"20283.10000\",\"0.00007767\",\"1656458258.935519\"],[\"20283.20000\",\"0.23151675\",\"1656458262.526219\"],[\"20285.30000\",\"0.25000000\",\"1656458263.956837\"]],\"bs\":[[\"20278.20000\",\"0.00202659\",\"1656458264.223238\"],[\"20278.10000\",\"0.01851865\",\"1656458263.795843\"],[\"20278.00000\",\"0.01039921\",\"1656458256.901625\"],[\"20277.60000\",\"0.11479452\",\"1656458264.122820\"],[\"20277.50000\",\"2.68731192\",\"1656458264.500913\"],[\"20277.40000\",\"0.01312144\",\"1656458256.161904\"],[\"20277.00000\",\"0.00324129\",\"1656458212.208035\"],[\"20275.10000\",\"0.28258250\",\"1656458255.667521\"],[\"20275.00000\",\"1.99358000\",\"1656458252.064366\"],[\"20273.80000\",\"4.93019866\",\"1656458257.644764\"]]},\"book-10\",\"XBT/USD\"]";

        snapshotOperator.processTradeMessage(testJsonStr);
        assertTrue(snapshotEthUsd.getAsks().isEmpty());
        assertTrue(snapshotEthUsd.getBids().isEmpty());
        assertEquals(10, snapshotBtcUsd.getAsks().size());
        assertEquals(10, snapshotBtcUsd.getBids().size());
    }

    @Test
    @Order(2)
    public void testCreateEthUsdSnapshot() {

        String testJsonStr = "[560,{\"as\":[[\"1144.55000\",\"85.76289693\",\"1656458263.803612\"],[\"1144.59000\",\"8.73779548\",\"1656458263.495953\"],[\"1144.64000\",\"40.20188887\",\"1656458263.915056\"],[\"1144.67000\",\"40.17157476\",\"1656458261.489672\"],[\"1144.85000\",\"3.61501686\",\"1656458262.990339\"],[\"1144.86000\",\"2.03572526\",\"1656458261.075273\"],[\"1144.88000\",\"26.03000000\",\"1656458262.270357\"],[\"1144.91000\",\"8.73573965\",\"1656458260.498313\"],[\"1144.94000\",\"3.85399439\",\"1656458261.542084\"],[\"1144.98000\",\"26.22000000\",\"1656458262.225016\"]],\"bs\":[[\"1144.54000\",\"0.10000000\",\"1656458260.400040\"],[\"1144.25000\",\"3.84659257\",\"1656458260.327781\"],[\"1144.15000\",\"3.51672438\",\"1656458260.558012\"],[\"1144.03000\",\"7.08636554\",\"1656458258.167485\"],[\"1144.02000\",\"3.61924075\",\"1656458258.211678\"],[\"1143.81000\",\"3.92313081\",\"1656458260.984608\"],[\"1143.80000\",\"3.54181360\",\"1656458255.500676\"],[\"1143.79000\",\"3.63373340\",\"1656458261.541413\"],[\"1143.61000\",\"27.44554901\",\"1656458264.029473\"],[\"1143.45000\",\"3.74212128\",\"1656458255.500566\"]]},\"book-10\",\"ETH/USD\"]";

        snapshotOperator.processTradeMessage(testJsonStr);
        assertEquals(10, snapshotEthUsd.getAsks().size());
        assertEquals(10, snapshotEthUsd.getBids().size());
        assertEquals(10, snapshotBtcUsd.getAsks().size());
        assertEquals(10, snapshotBtcUsd.getBids().size());
        assertEquals(7.08636554D, snapshotEthUsd.getBids().get(1144.03).getAmount());
        snapshotOperator.dumpAllSnapshots(System.out);
    }

    @Test
    @Order(3)
    public void testUpdateBid() {
        String testJsonStr = "[560,{\"b\":[[\"1144.03000\",\"37.67105113\",\"1656458264.629331\"]],\"c\":\"89931786\"},\"book-10\",\"ETH/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);

        assertEquals(37.67105113D, snapshotEthUsd.getBids().get(1144.03).getAmount());
        assertEquals(LocalDateTime.ofEpochSecond(1656458264L, 629331, ZoneOffset.UTC), snapshotEthUsd.getLatestTimestamp());

//        snapshotOperator.dumpAllSnapshots(System.out); // Just for info
    }

    @Test
    @Order(4)
    public void testCreateBid() {
        assertEquals(1143.45, snapshotEthUsd.getBids().lastKey());
        String testJsonStr = "[560,{\"b\":[[\"1144.04000\",\"30.58454972\",\"1656458265.133296\"]],\"c\":\"3291298277\"},\"book-10\",\"ETH/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);
        assertTrue(snapshotEthUsd.getBids().containsKey(1144.04));
        assertEquals(1143.61, snapshotEthUsd.getBids().lastKey());
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 133296, ZoneOffset.UTC), snapshotEthUsd.getLatestTimestamp());
    }

    @Test
    @Order(5)
    public void testDeleteBid() {
        String testJsonStr = "[560,{\"b\":[[\"1144.04000\",\"0.00000000\",\"1656458265.227062\"],[\"1143.62000\",\"1.22554901\",\"1656458264.674373\",\"r\"]],\"c\":\"1267745156\"},\"book-10\",\"ETH/USD\"]";

        snapshotOperator.processTradeMessage(testJsonStr);

        assertFalse(snapshotEthUsd.getBids().containsKey(1144.04D));
        assertEquals(1.22554901D, snapshotEthUsd.getBids().get(1143.62D).getAmount());
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 227062, ZoneOffset.UTC), snapshotEthUsd.getLatestTimestamp());

//        snapshotOperator.dumpAllSnapshots(System.out);
    }

    @Test
    @Order(6)
    /**
     * An unlikely scenario in the real world, but let's test it just in case
     */
    public void testEnterExit() {
        String testJsonStr = "[560,{\"b\":[[\"999.04000\",\"1.23000000\",\"1656458265.227062\"]],\"c\":\"1267745156\"},\"book-10\",\"ETH/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);

        assertEquals(1143.61, snapshotEthUsd.getBids().lastKey());
        assertFalse(snapshotEthUsd.getBids().containsKey(999.04));
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 227062, ZoneOffset.UTC), snapshotEthUsd.getLatestTimestamp());
    }

    @Test
    @Order(7)
    public void testUpdateAsk() {
        assertTrue(snapshotBtcUsd.getAsks().containsKey(20281.5D));
        assertNotEquals(0.26505876D, snapshotBtcUsd.getAsks().get(20281.5D).getAmount());

        String testJsonStr = "[336,{\"a\":[[\"20281.50000\",\"0.26505876\",\"1656458265.236761\"]],\"c\":\"2317773838\"},\"book-10\",\"XBT/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);
        assertEquals(0.26505876D, snapshotBtcUsd.getAsks().get(20281.5D).getAmount());
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 236761, ZoneOffset.UTC), snapshotBtcUsd.getLatestTimestamp());
    }

    @Test
    @Order(8)
    public void testCreateAsk() {
        assertTrue(snapshotBtcUsd.getAsks().containsKey(20282.1D));
        assertFalse(snapshotBtcUsd.getAsks().containsKey(20287.3D));
        String testJsonStr = "[336,{\"a\":[[\"20282.10000\",\"0.00000000\",\"1656458265.240169\"],[\"20287.30000\",\"0.15733892\",\"1656458257.354017\",\"r\"]],\"c\":\"451844366\"},\"book-10\",\"XBT/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);
        assertFalse(snapshotBtcUsd.getAsks().containsKey(20282.1D));
        assertEquals(0.15733892D, snapshotBtcUsd.getAsks().get(20287.3D).getAmount());
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 240169, ZoneOffset.UTC), snapshotBtcUsd.getLatestTimestamp());
    }

    @Test
    @Order(9)
    public void testDeleteAsk() {
        assertTrue(snapshotBtcUsd.getAsks().containsKey(20280.1D));
        String testJsonStr = "[336,{\"a\":[[\"20280.10000\",\"0.00000000\",\"1656458265.256818\"],[\"20285.50000\",\"0.56308000\",\"1656458250.356237\",\"r\"]],\"c\":\"2434637797\"},\"book-10\",\"XBT/USD\"]";
        snapshotOperator.processTradeMessage(testJsonStr);

        assertFalse(snapshotBtcUsd.getAsks().containsKey(20280.1D));
        assertEquals(0.56308D, snapshotBtcUsd.getAsks().get(20285.5D).getAmount());
        assertEquals(LocalDateTime.ofEpochSecond(1656458265L, 256818, ZoneOffset.UTC), snapshotBtcUsd.getLatestTimestamp());
    }
}
