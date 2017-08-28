package com.blokaly.ceres.orderbook

import com.blokaly.ceres.common.DecimalNumber
import com.blokaly.ceres.data.MarketDataIncremental
import com.blokaly.ceres.data.OrderInfo
import com.blokaly.ceres.gdax.GdaxMDSnapshot
import spock.lang.Specification

class OrderBookTest extends Specification {

    def 'Add new orders to order book'() {

        setup:
        def orderBook = new OrderBasedOrderBook("test")
        orderBook.lastSequence = 1
        def seq = 2;
        def mockMarketData1 = buildNewMarketData(seq++, o1, OrderInfo.Side.BUY, 1000.23D, q1)
        def mockMarketData2 = buildNewMarketData(seq++, o2, OrderInfo.Side.BUY, 1000.23D, q2)

        when:
        orderBook.processIncrementalUpdate(mockMarketData1)
        orderBook.processIncrementalUpdate(mockMarketData2)

        then:
        orderBook.getBids().size() == 1
        orderBook.getAsks().size() == 0
        OrderBook.Level level = orderBook.getBids().toArray()[0]
        level.getPrice().asDbl() == 1000.23D
        level.getQuantity().asDbl() == q

        where:
        o1    | q1    | o2    | q2    || q
        "id1" | 1.45D | "id2" | 1.55D || 3.0D
    }

    def 'Update order in order book'() {

        setup:
        def orderBook = new OrderBasedOrderBook("test")
        orderBook.lastSequence = 1
        def seq = 2;
        def mockMarketData1 = buildNewMarketData(seq++, o1, OrderInfo.Side.BUY, 1000.23D, q1)
        def mockMarketData2 = buildNewMarketData(seq++, o2, OrderInfo.Side.BUY, 1000.33D, 1.55D)
        def mockMarketData3 = buildUpdateMarketData(seq++, o2, OrderInfo.Side.BUY, 1000.33D, q2)

        when:
        orderBook.processIncrementalUpdate(mockMarketData1)
        orderBook.processIncrementalUpdate(mockMarketData2)
        orderBook.processIncrementalUpdate(mockMarketData3)

        then:
        orderBook.getBids().size() == 2
        OrderBook.Level level1 = orderBook.getBids().toArray()[0]
        OrderBook.Level level2 = orderBook.getBids().toArray()[1]
        level1.getQuantity().asDbl() == lev1
        level2.getQuantity().asDbl() == lev2

        where:
        o1    | q1    | o2    | q2   || lev1   | lev2
        "id1" | 1.45D | "id2" | 1.0D || 1.0D | 1.45D
    }

    def 'Remove order from order book'() {

        setup:
        def orderBook = new OrderBasedOrderBook("test")
        orderBook.lastSequence = 1
        def seq = 2;
        def mockMarketData1 = buildNewMarketData(seq++, o1, OrderInfo.Side.BUY, 1000.23D, q1)
        def mockMarketData2 = buildNewMarketData(seq++, o2, OrderInfo.Side.BUY, 1000.33D, q2)
        def mockMarketData3 = buildDoneMarketData(seq++, o2, OrderInfo.Side.BUY, 1000.33D)

        when:
        orderBook.processIncrementalUpdate(mockMarketData1)
        orderBook.processIncrementalUpdate(mockMarketData2)
        orderBook.processIncrementalUpdate(mockMarketData3)

        then:
        orderBook.getBids().size() == 1
        OrderBook.Level level = orderBook.getBids().toArray()[0]
        level.getPrice().asDbl() == 1000.23D

        where:
        o1    | q1    | o2    | q2
        "id1" | 1.45D | "id2" | 1.0D
    }

    def 'Process full order book json data'() {
        setup:
        def json = """{
                    |"sequence":3870056379,
                    |"bids":[
                    |["4096.5","10.23261257","54188e78-ae98-4e63-98fe-a0482f32faee"],
                    |["4096","0.5","2c8216e6-75c8-469b-b627-079366ab2551"],
                    |["4094.65","0.03","d10270a3-3bb6-4151-965c-bcda3ab6a543"]],
                    |"asks":[["4096.88","5","627ab533-1cee-447c-8f2a-5f50389c46e1"],
                    |["4096.89","5.94858259","812a1daa-4520-4f83-a9e5-5529d27928fb"],
                    |["4096.89","0.51848649","12f187e0-a72c-4021-ab7a-aa3e5d263988"]]
                    |}""".stripMargin('|').replaceAll("\\n", " ")

        def orderBook = new OrderBasedOrderBook("test")

        when:
        def snapshot = GdaxMDSnapshot.parse(json)
        orderBook.processSnapshot(snapshot);

        then:
        orderBook.getBids().size() == 3
        orderBook.getAsks().size() == 2
        orderBook.toString() == "OrderBook{test, bids=[ [4096.5,10.23261257] [4096,0.5] [4094.65,0.03] ], asks=[ [4096.88,5] [4096.89,6.46706908] ]}"
    }

    def buildNewMarketData(long seq, id, side, double price, double quantity) {
        def mockMarketData = Mock(MarketDataIncremental)
        def mockOrderInfo = Mock(OrderInfo)
        mockMarketData.getSequence() >> seq
        mockMarketData.type() >> MarketDataIncremental.Type.NEW
        mockMarketData.orderInfo() >> mockOrderInfo
        mockOrderInfo.getId() >> id
        mockOrderInfo.side() >> side
        mockOrderInfo.getPrice() >> DecimalNumber.fromDbl(price)
        mockOrderInfo.getQuantity() >> DecimalNumber.fromDbl(quantity)
        mockMarketData;
    }

    def buildUpdateMarketData(long seq, id, side, double price, double quantity) {
        def mockMarketData = Mock(MarketDataIncremental)
        def mockOderInfo = Mock(OrderInfo)
        mockMarketData.getSequence() >> seq
        mockMarketData.type() >> MarketDataIncremental.Type.UPDATE
        mockMarketData.orderInfo() >> mockOderInfo
        mockOderInfo.getId() >> id
        mockOderInfo.side() >> side
        mockOderInfo.getPrice() >> DecimalNumber.fromDbl(price)
        mockOderInfo.getQuantity() >> DecimalNumber.fromDbl(quantity)
        mockMarketData;
    }

    def buildDoneMarketData(long seq, id, side, double price) {
        def mockMarketData = Mock(MarketDataIncremental)
        def mockOrderInfo = Mock(OrderInfo)
        mockMarketData.getSequence() >> seq
        mockMarketData.type() >> MarketDataIncremental.Type.DONE
        mockMarketData.orderInfo() >> mockOrderInfo
        mockOrderInfo.getId() >> id
        mockOrderInfo.side() >> side
        mockOrderInfo.getPrice() >> DecimalNumber.fromDbl(price)
        mockMarketData;
    }
}
