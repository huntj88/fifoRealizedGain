package me.jameshunt.fifo

import kotlin.math.absoluteValue

infix fun Double.to(that: Double): Fifo.Transaction = Fifo.Transaction(this, that)

class Fifo(private val purchased: List<Transaction>, private val sold: List<Transaction>) {

    fun findRealizedGain(): Double {
        return purchased
                .fold(Pair(0.0, sold)) { acc, transaction ->
                    val leftOver = useOnePurchase(transaction, acc.second)
                    val resultFromOnePass = when (leftOver) {
                        is LeftOver.PurchaseLeftOver -> Pair(leftOver.gain, listOf())
                        is LeftOver.SoldLeftOver -> Pair(leftOver.gain, leftOver.remainingSold)
                    }
                    Pair(acc.first + resultFromOnePass.first, resultFromOnePass.second)
                }.first
    }

    internal fun useOnePurchase(purchase: Transaction, remainingSold: List<Transaction>, gain: Double = 0.0): LeftOver {

        val sold = remainingSold.firstOrNull() ?: return LeftOver.PurchaseLeftOver(purchase, gain)

        val leftOver = this.useOneSaleOnPurchase(purchase = purchase, sold = sold)

        return when (leftOver) {
            is LeftOverOneEach.PurchaseLeftOver -> this.useOnePurchase(
                    purchase = leftOver.purchase,
                    remainingSold = remainingSold.removeFirst(),
                    gain = leftOver.gain + gain
            )
            is LeftOverOneEach.SoldLeftOver -> {
                val newRemainingSold = listOf(leftOver.sold) + remainingSold.removeFirst()
                LeftOver.SoldLeftOver(
                        remainingSold = newRemainingSold,
                        gain = leftOver.gain + gain
                )
            }
            is LeftOverOneEach.BothUsed -> LeftOver.SoldLeftOver(
                    remainingSold = remainingSold.removeFirst(),
                    gain = leftOver.gain + gain
            )
        }
    }

    private fun List<Transaction>.removeFirst(): List<Transaction> = this.subList(1, this.size)

    internal sealed class LeftOver {
        data class PurchaseLeftOver(val purchase: Transaction, val gain: Double) : LeftOver()
        data class SoldLeftOver(val remainingSold: List<Transaction>, val gain: Double) : LeftOver()
    }


    internal fun useOneSaleOnPurchase(purchase: Transaction, sold: Transaction): LeftOverOneEach {
        val itemsLeft = purchase.items - sold.items

        return when {
            itemsLeft == 0.0 -> LeftOverOneEach.BothUsed(gain = sold.currencyAmount - purchase.currencyAmount)
            itemsLeft > 0 -> {
                val purchaseLeftOver = Transaction(transaction = purchase, itemsLeft = itemsLeft)
                val numSold = purchase.items - purchaseLeftOver.items
                val gain = (sold.currencyPerUnit - purchase.currencyPerUnit) * numSold

                LeftOverOneEach.PurchaseLeftOver(purchase = purchaseLeftOver, gain = gain)
            }
            itemsLeft < 0 -> {
                val soldLeftOver = Transaction(sold, itemsLeft.absoluteValue)
                val numSold = sold.items - soldLeftOver.items
                val gain = (sold.currencyPerUnit - purchase.currencyPerUnit) * numSold

                LeftOverOneEach.SoldLeftOver(sold = soldLeftOver, gain = gain)
            }
            else -> throw IllegalStateException()
        }
    }

    internal sealed class LeftOverOneEach {
        data class PurchaseLeftOver(val purchase: Transaction, val gain: Double) : LeftOverOneEach()
        data class SoldLeftOver(val sold: Transaction, val gain: Double) : LeftOverOneEach()
        data class BothUsed(val gain: Double) : LeftOverOneEach()
    }

    data class Transaction(
            val items: Double,
            val currencyAmount: Double
    ) {
        constructor(transaction: Transaction, itemsLeft: Double) : this(
                items = itemsLeft,
                currencyAmount = transaction.currencyAmount * itemsLeft / transaction.items
        )

        val currencyPerUnit: Double
            get() = this.currencyAmount / items
    }
}
