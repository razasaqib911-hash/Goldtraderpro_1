package com.example.data.model

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0
) {
    val isBullish: Boolean get() = close >= open
    val isBearish: Boolean get() = close < open
    val bodyTop: Double get() = maxOf(open, close)
    val bodyBottom: Double get() = minOf(open, close)
    val range: Double get() = high - low
    val bodySize: Double get() = kotlin.math.abs(close - open)
}
