package com.blokaly.ceres.gdax

import com.blokaly.ceres.data.MarketDataIncremental
import com.blokaly.ceres.data.OrderInfo
import spock.lang.Specification

class CBMarketDataTest extends Specification {

    def 'Process open json data' () {
        setup:
        def json = """{
                    |"type": "open",
                    |"time": "2014-11-07T08:19:27.028459Z",
                    |"product_id": "BTC-USD",
                    |"sequence": 10,
                    |"order_id": "d50ec984-77a8-460a-b958-66f114b0de9b",
                    |"price": "200.2",
                    |"remaining_size": "1.00",
                    |"side": "sell"
                    |}""".stripMargin('|').replaceAll("\\n", " ")

        when:
        def marketdata = GdaxMDIncremental.parse(json)

        then:
        marketdata.type() == MarketDataIncremental.Type.NEW
        marketdata.getSequence() == 10
        def order = marketdata.orderInfo()
        order.getId() == "d50ec984-77a8-460a-b958-66f114b0de9b"
        order.side() == OrderInfo.Side.SELL
        order.getPrice().asDbl() == 200.2D;
        order.getQuantity().asDbl() == 1.0D;
    }

    def 'Process done json data' () {
        setup:
        def json = """{
                    |"type": "done",
                    |"time": "2014-11-07T08:19:27.028459Z",
                    |"product_id": "BTC-USD",
                    |"sequence": 11,
                    |"price": "200.2",
                    |"order_id": "d50ec984-77a8-460a-b958-66f114b0de9b",
                    |"reason": "filled",
                    |"side": "sell",
                    |"remaining_size": "0.2"
                    |}""".stripMargin('|').replaceAll("\\n", " ")

        when:
        def marketdata = GdaxMDIncremental.parse(json)

        then:
        marketdata.type() == MarketDataIncremental.Type.DONE
        marketdata.getSequence() == 11
        def order = marketdata.orderInfo()
        order.getId() == "d50ec984-77a8-460a-b958-66f114b0de9b"
        order.side() == OrderInfo.Side.SELL
        order.getPrice().asDbl() == 200.2D;
        order.getQuantity().asDbl() == 0.2D;
    }

    def 'Process size change json data' () {
        setup:
        def json = """{
                    |"type": "change",
                    |"time": "2014-11-07T08:19:27.028459Z",
                    |"sequence": 80,
                    |"order_id": "ac928c66-ca53-498f-9c13-a110027a60e8",
                    |"product_id": "BTC-USD",
                    |"new_size": "5.23512",
                    |"old_size": "12.234412",
                    |"price": "400.23",
                    |"side": "sell"
                    |}""".stripMargin('|').replaceAll("\\n", " ")

        when:
        def marketdata = GdaxMDIncremental.parse(json)

        then:
        marketdata.type() == MarketDataIncremental.Type.UPDATE
        marketdata.getSequence() == 80
        def order = marketdata.orderInfo()
        order.getId() == "ac928c66-ca53-498f-9c13-a110027a60e8"
        order.side() == OrderInfo.Side.SELL
        order.getPrice().asDbl() == 400.23D;
        order.getQuantity().asDbl() == 5.23512D;
    }

    def 'Process fund change json data' () {
        setup:
        def json = """{
                    |"type": "change",
                    |"time": "2014-11-07T08:19:27.028459Z",
                    |"sequence": 81,
                    |"order_id": "ac928c66-ca53-498f-9c13-a110027a60e8",
                    |"product_id": "BTC-USD",
                    |"new_funds": "5.23512",
                    |"old_funds": "12.234412",
                    |"price": "400.23",
                    |"side": "sell"
                    |}""".stripMargin('|').replaceAll("\\n", " ")

        when:
        def marketdata = GdaxMDIncremental.parse(json)

        then:
        marketdata.type() == MarketDataIncremental.Type.UNKNOWN
        marketdata.getSequence() == 81
        def order = marketdata.orderInfo()
        order == null
    }
}
