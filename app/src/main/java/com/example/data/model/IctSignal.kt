package com.example.data.model

enum class Direction {
    BULLISH,
    BEARISH,
    NEUTRAL
}

enum class RangeZone {
    PREMIUM,    // Above 50% Equilibrium -> Look for SELLS
    DISCOUNT,   // Below 50% Equilibrium -> Look for BUYS
    EQUILIBRIUM
}

enum class SweepType {
    BUY_SIDE_LIQUIDITY,  // BSL Swept (Bearish reversal signal)
    SELL_SIDE_LIQUIDITY  // SSL Swept (Bullish reversal signal)
}

enum class KillzoneType(val label: String, val startUtcHour: Int, val endUtcHour: Int) {
    LONDON("London Killzone", 7, 10),
    NEW_YORK("New York Killzone", 12, 15),
    ASIAN("Asian Session", 0, 4),
    OFF_SESSION("Off Session", -1, -1)
}

data class DealingRange(
    val high: Double,
    val low: Double,
    val equilibrium: Double,
    val currentPrice: Double,
    val zone: RangeZone
)

data class LiquiditySweep(
    val type: SweepType,
    val sweptLevelPrice: Double,
    val peakPrice: Double,
    val label: String,
    val timestamp: Long
)

data class MarketStructureShift(
    val direction: Direction,
    val brokenLevel: Double,
    val displacementCandleClose: Double,
    val timestamp: Long
)

data class FairValueGap(
    val id: String,
    val direction: Direction,
    val top: Double,
    val bottom: Double,
    val midpointCe: Double, // Consequent Encroachment (50%)
    val timestamp: Long,
    val isFilled: Boolean = false,
    val isRetraced: Boolean = false
)

data class IctPipelineStatus(
    val step: Int, // 1 to 6
    val title: String,
    val detail: String,
    val htfBias: Direction,
    val dealingRange: DealingRange?,
    val latestSweep: LiquiditySweep?,
    val activeMss: MarketStructureShift?,
    val activeFvg: FairValueGap?,
    val killzone: KillzoneType,
    val isKillzoneValid: Boolean,
    val isDiscountPremiumAligned: Boolean,
    val isSignalActive: Boolean
)
