package me.jameshunt.fifo

import org.junit.Assert
import org.junit.Test

infix fun Double.to(that: Double): Fifo.Transaction = Fifo.Transaction(this, that)

class FifoTest {

    private val purchased = listOf(
            2.0 to 20.0,
            20.0 to 300.84,
            4.0 to 100.0,
            3.0 to 58.93,
            3.0 to 64.54,
            17.0 to 210.11,
            12.0 to 137.48,
            1.0 to 32.00,
            1.0 to 12.9
    )

    private val sold = listOf(
            2.0 to 10.0,
            14.0 to 200.84,
            16.0 to 300.0,
            3.0 to 48.93,
            4.0 to 84.54,
            6.0 to 110.11,
            2.0 to 37.48,
            1.0 to 22.00,
            1.0 to 4.9
    )


    @Test
    fun findRealizedGainTest() {
        // purchased.printTotal()
        // sold.printTotal()

        val realizedGainAndLeftOver = Fifo.findRealizedGain(purchased, sold)
        Assert.assertEquals(64.38, realizedGainAndLeftOver.gainSoFar, 0.1)
    }

    @Test
    fun useOnePurchaseTest() {

        val one = Fifo.useOnePurchase(4.0 to 20.0, listOf(2.0 to 15.0, 2.0 to 15.0)) as Fifo.LeftOver.SoldLeftOver
        Assert.assertEquals(10.0, one.gain, 0.0001) // should be gain of 10

        val two = Fifo.useOnePurchase(5.0 to 20.0, listOf(2.0 to 15.0, 2.0 to 15.0)) as Fifo.LeftOver.PurchaseLeftOver
        Assert.assertEquals(14.0, two.gain, 0.0001) // should be gain of 14

        val three = Fifo.useOnePurchase(10.0 to 20.0, listOf(2.0 to 15.0, 6.0 to 50.0)) as Fifo.LeftOver.PurchaseLeftOver
        Assert.assertEquals(49.0, three.gain, 0.0001) // should be gain of 49

        val four = Fifo.useOnePurchase(8.0 to 20.0, listOf(2.0 to 15.0, 6.0 to 50.0)) as Fifo.LeftOver.SoldLeftOver
        Assert.assertEquals(45.0, four.gain, 0.0001) // should be gain of 45

        val five = Fifo.useOnePurchase(8.0 to 80.0, listOf(2.0 to 15.0, 6.0 to 50.0)) as Fifo.LeftOver.SoldLeftOver
        Assert.assertEquals(-15.0, five.gain, 0.0001) // should be gain of -15

        val six = Fifo.useOnePurchase(10.0 to 80.0, listOf(2.0 to 15.0, 6.7 to 50.0)) as Fifo.LeftOver.PurchaseLeftOver
        Assert.assertEquals(-4.6, six.gain, 0.0001) // should be gain of -4.6
    }

    @Test
    fun useOneSaleOnPurchaseTest() {

        val one = Fifo.useOneSaleOnPurchase(2.0 to 20.0, 3.0 to 70.0) as Fifo.LeftOverOneSale.SoldLeftOver
        Assert.assertEquals(26.6666, one.gain, 0.0001) // should be gain of 26.6666

        val two = Fifo.useOneSaleOnPurchase(4.0 to 20.0, 3.0 to 70.0) as Fifo.LeftOverOneSale.PurchaseLeftOver
        Assert.assertEquals(55.00, two.gain, 0.0001) // should be gain of 55.0

        val three = Fifo.useOneSaleOnPurchase(4.0 to 20.0, 3.0 to 18.0) as Fifo.LeftOverOneSale.PurchaseLeftOver
        Assert.assertEquals(3.0, three.gain, 0.0001) // should be gain of 3.0

        val four = Fifo.useOneSaleOnPurchase(4.0 to 20.0, 3.0 to 12.0) as Fifo.LeftOverOneSale.PurchaseLeftOver
        Assert.assertEquals(-3.0, four.gain, 0.0001) // should be gain of 3.0
    }

    private fun List<Fifo.Transaction>.printTotal() {
        this.reduce { acc, pair ->
            Fifo.Transaction(pair.items + acc.items, pair.currencyAmount + acc.currencyAmount)
        }.also { print(it) }
    }
}
