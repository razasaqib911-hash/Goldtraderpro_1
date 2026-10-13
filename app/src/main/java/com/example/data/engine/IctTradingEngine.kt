package com.example.data.engine

import com.example.data.model.Candle
import com.example.data.model.DealingRange
import com.example.data.model.Direction
import com.example.data.model.FairValueGap
import com.example.data.model.IctPipelineStatus
import com.example.data.model.KillzoneType
import com.example.data.model.LiquiditySweep
import com.example.data.model.MarketStructureShift
import com.example.data.model.RangeZone
import com.example.data.model.SweepType
import com.example.data.model.TradePosition
import com.example.data.model.TradeType
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID
import kotlin.math.abs

data class AutoTradeTrigger(
    val type: TradeType,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val reason: String
)

class IctTradingEngine {

    /**
     * Complete ICT Analysis Pipeline executed on the candle series.
     */
    fun analyze(
        candles: List<Candle>,
        currentPrice: Double,
        riskRewardRatio: Double = 2.0,
        enforceKillzones: Boolean = false
    ): Pair<IctPipelineStatus, AutoTradeTrigger?> {
        if (candles.size < 20) {
            val emptyStatus = IctPipelineStatus(
                step = 1,
                title = "1. HTF Bias & IPDA Lookback",
                detail = "Collecting candle history for IPDA 20/40/60 lookback...",
                htfBias = Direction.NEUTRAL,
                dealingRange = null,
                latestSweep = null,
                activeMss = null,
                activeFvg = null,
                killzone = getCurrentKillzone(),
                isKillzoneValid = true,
                isDiscountPremiumAligned = false,
                isSignalActive = false
            )
            return Pair(emptyStatus, null)
        }

        // STEP 1: HTF Bias & IPDA Reference Levels (20, 40, 60 lookbacks)
        val ipda20High = candles.takeLast(20).maxOf { it.high }
        val ipda20Low = candles.takeLast(20).minOf { it.low }
        val ipda40High = if (candles.size >= 40) candles.takeLast(40).maxOf { it.high } else ipda20High
        val ipda40Low = if (candles.size >= 40) candles.takeLast(40).minOf { it.low } else ipda20Low
        val ipdaMidpoint = (ipda20High + ipda20Low + ipda40High + ipda40Low) / 4.0

        val htfBias = if (currentPrice > ipdaMidpoint) Direction.BULLISH else Direction.BEARISH

        // STEP 2: Premium vs. Discount Arrays (Dealing Range 50% Equilibrium)
        val dealingRangeHigh = ipda20High
        val dealingRangeLow = ipda20Low
        val equilibrium = (dealingRangeHigh + dealingRangeLow) / 2.0
        val currentZone = when {
            currentPrice < equilibrium -> RangeZone.DISCOUNT
            currentPrice > equilibrium -> RangeZone.PREMIUM
            else -> RangeZone.EQUILIBRIUM
        }
        val dealingRange = DealingRange(
            high = dealingRangeHigh,
            low = dealingRangeLow,
            equilibrium = equilibrium,
            currentPrice = currentPrice,
            zone = currentZone
        )

        // STEP 3: Liquidity Sweep Detection (ERL - External Range Liquidity)
        // Find recent swing highs and lows in past 15 candles
        val swingHighs = mutableListOf<Pair<Int, Double>>()
        val swingLows = mutableListOf<Pair<Int, Double>>()
        for (i in 2 until candles.size - 2) {
            val c = candles[i]
            if (c.high > candles[i - 1].high && c.high > candles[i - 2].high &&
                c.high > candles[i + 1].high && c.high > candles[i + 2].high
            ) {
                swingHighs.add(i to c.high)
            }
            if (c.low < candles[i - 1].low && c.low < candles[i - 2].low &&
                c.low < candles[i + 1].low && c.low < candles[i + 2].low
            ) {
                swingLows.add(i to c.low)
            }
        }

        var latestSweep: LiquiditySweep? = null
        // Check if recent candles pierced swing high/low with wicks
        val recentCandles = candles.takeLast(8)
        for (c in recentCandles) {
            for ((_, sh) in swingHighs.takeLast(3)) {
                if (c.high > sh && c.close < sh) {
                    latestSweep = LiquiditySweep(
                        type = SweepType.BUY_SIDE_LIQUIDITY,
                        sweptLevelPrice = sh,
                        peakPrice = c.high,
                        label = "BSL Swept (Buy-Side Liquidity)",
                        timestamp = c.timestamp
                    )
                }
            }
            for ((_, sl) in swingLows.takeLast(3)) {
                if (c.low < sl && c.close > sl) {
                    latestSweep = LiquiditySweep(
                        type = SweepType.SELL_SIDE_LIQUIDITY,
                        sweptLevelPrice = sl,
                        peakPrice = c.low,
                        label = "SSL Swept (Sell-Side Liquidity)",
                        timestamp = c.timestamp
                    )
                }
            }
        }

        // STEP 4: Session Timings / Killzones Filter
        val currentKillzone = getCurrentKillzone()
        val isKillzoneValid = if (enforceKillzones) {
            currentKillzone == KillzoneType.LONDON || currentKillzone == KillzoneType.NEW_YORK
        } else {
            true // Allow demo / anytime testing
        }

        // STEP 5: Market Structure Shift (MSS) with Candle Displacement
        var activeMss: MarketStructureShift? = null
        var lastSwingPointForSl = dealingRangeLow

        if (candles.size >= 5) {
            val lastCandle = candles.last()
            val prevCandle = candles[candles.size - 2]
            val avgRange = candles.takeLast(10).map { it.range }.average().coerceAtLeast(0.0001)

            // Bullish MSS: displacement candle breaks above recent swing high with body closure
            val recentSwingHigh = swingHighs.lastOrNull()?.second
            if (recentSwingHigh != null && lastCandle.isBullish && lastCandle.close > recentSwingHigh && lastCandle.bodySize > avgRange * 0.9) {
                activeMss = MarketStructureShift(
                    direction = Direction.BULLISH,
                    brokenLevel = recentSwingHigh,
                    displacementCandleClose = lastCandle.close,
                    timestamp = lastCandle.timestamp
                )
                lastSwingPointForSl = swingLows.lastOrNull()?.second ?: lastCandle.low
            }

            // Bearish MSS: displacement candle breaks below recent swing low with body closure
            val recentSwingLow = swingLows.lastOrNull()?.second
            if (recentSwingLow != null && lastCandle.isBearish && lastCandle.close < recentSwingLow && lastCandle.bodySize > avgRange * 0.9) {
                activeMss = MarketStructureShift(
                    direction = Direction.BEARISH,
                    brokenLevel = recentSwingLow,
                    displacementCandleClose = lastCandle.close,
                    timestamp = lastCandle.timestamp
                )
                lastSwingPointForSl = swingHighs.lastOrNull()?.second ?: lastCandle.high
            }
        }

        // STEP 6: Fair Value Gap (FVG) Detection (3-candle imbalance)
        val activeFvgs = detectFvgs(candles)
        val activeFvg = activeFvgs.lastOrNull { !it.isFilled }

        // Pipeline state progression
        val isAlignedDiscountBuy = (currentZone == RangeZone.DISCOUNT && htfBias == Direction.BULLISH)
        val isAlignedPremiumSell = (currentZone == RangeZone.PREMIUM && htfBias == Direction.BEARISH)
        val isDiscountPremiumAligned = isAlignedDiscountBuy || isAlignedPremiumSell

        var pipelineStep = 1
        var statusTitle = "1. HTF Bias & IPDA Lookback"
        var statusDetail = "HTF Direction: ${htfBias.name} | Midpoint: ${String.format("%.4f", ipdaMidpoint)}"

        if (isDiscountPremiumAligned) {
            pipelineStep = 2
            statusTitle = "2. Dealing Range Equilibrium"
            statusDetail = if (currentZone == RangeZone.DISCOUNT) {
                "In DISCOUNT (${String.format("%.4f", currentPrice)} < ${String.format("%.4f", equilibrium)}). Valid for BUYS."
            } else {
                "In PREMIUM (${String.format("%.4f", currentPrice)} > ${String.format("%.4f", equilibrium)}). Valid for SELLS."
            }
        }

        if (latestSweep != null) {
            pipelineStep = 3
            statusTitle = "3. Liquidity Sweep Confirmed"
            statusDetail = "${latestSweep.label} at ${String.format("%.4f", latestSweep.sweptLevelPrice)}"
        }

        if (!isKillzoneValid) {
            statusTitle = "4. Session Killzone Paused"
            statusDetail = "Outside London/NY Killzones (${currentKillzone.label}). Awaiting active window."
        } else if (pipelineStep >= 3) {
            pipelineStep = 4
            statusTitle = "4. Killzone Active"
            statusDetail = "${currentKillzone.label} open. Scanning Lower Timeframe MSS."
        }

        if (activeMss != null && isKillzoneValid) {
            pipelineStep = 5
            statusTitle = "5. Market Structure Shift (MSS)"
            statusDetail = "${activeMss.direction.name} displacement broke ${String.format("%.4f", activeMss.brokenLevel)}"
        }

        var trigger: AutoTradeTrigger? = null

        if (activeFvg != null && isKillzoneValid) {
            // Check if current price is inside or retracing into the FVG
            val isRetracingBullish = activeFvg.direction == Direction.BULLISH && currentPrice <= activeFvg.top && currentPrice >= activeFvg.bottom
            val isRetracingBearish = activeFvg.direction == Direction.BEARISH && currentPrice >= activeFvg.bottom && currentPrice <= activeFvg.top

            if (isRetracingBullish || isRetracingBearish) {
                pipelineStep = 6
                statusTitle = "6. FVG Retracement Entry"
                statusDetail = "Price entered ${activeFvg.direction.name} FVG [${String.format("%.4f", activeFvg.bottom)} - ${String.format("%.4f", activeFvg.top)}]"

                if (isRetracingBullish && currentZone == RangeZone.DISCOUNT) {
                    val sl = lastSwingPointForSl.coerceAtMost(currentPrice - 0.0001)
                    val risk = abs(currentPrice - sl).coerceAtLeast(0.0001)
                    val tp = currentPrice + (risk * riskRewardRatio)
                    trigger = AutoTradeTrigger(
                        type = TradeType.BUY,
                        entryPrice = currentPrice,
                        stopLoss = sl,
                        takeProfit = tp,
                        reason = "ICT FVG Retracement in Discount Zone"
                    )
                } else if (isRetracingBearish && currentZone == RangeZone.PREMIUM) {
                    val sl = lastSwingPointForSl.coerceAtLeast(currentPrice + 0.0001)
                    val risk = abs(sl - currentPrice).coerceAtLeast(0.0001)
                    val tp = currentPrice - (risk * riskRewardRatio)
                    trigger = AutoTradeTrigger(
                        type = TradeType.SELL,
                        entryPrice = currentPrice,
                        stopLoss = sl,
                        takeProfit = tp,
                        reason = "ICT FVG Retracement in Premium Zone"
                    )
                }
            }
        }

        val pipelineStatus = IctPipelineStatus(
            step = pipelineStep,
            title = statusTitle,
            detail = statusDetail,
            htfBias = htfBias,
            dealingRange = dealingRange,
            latestSweep = latestSweep,
            activeMss = activeMss,
            activeFvg = activeFvg,
            killzone = currentKillzone,
            isKillzoneValid = isKillzoneValid,
            isDiscountPremiumAligned = isDiscountPremiumAligned,
            isSignalActive = trigger != null
        )

        return Pair(pipelineStatus, trigger)
    }

    /**
     * Identifies 3-candle Fair Value Gaps across the chart history.
     */
    fun detectFvgs(candles: List<Candle>): List<FairValueGap> {
        val fvgs = mutableListOf<FairValueGap>()
        if (candles.size < 3) return fvgs

        for (i in 2 until candles.size) {
            val c1 = candles[i - 2]
            val c2 = candles[i - 1]
            val c3 = candles[i]

            // Bullish FVG: Candle 1 High < Candle 3 Low
            if (c2.isBullish && c1.high < c3.low) {
                val top = c3.low
                val bottom = c1.high
                val midpoint = (top + bottom) / 2.0
                fvgs.add(
                    FairValueGap(
                        id = "fvg_bull_${c2.timestamp}",
                        direction = Direction.BULLISH,
                        top = top,
                        bottom = bottom,
                        midpointCe = midpoint,
                        timestamp = c2.timestamp
                    )
                )
            }

            // Bearish FVG: Candle 1 Low > Candle 3 High
            if (c2.isBearish && c1.low > c3.high) {
                val top = c1.low
                val bottom = c3.high
                val midpoint = (top + bottom) / 2.0
                fvgs.add(
                    FairValueGap(
                        id = "fvg_bear_${c2.timestamp}",
                        direction = Direction.BEARISH,
                        top = top,
                        bottom = bottom,
                        midpointCe = midpoint,
                        timestamp = c2.timestamp
                    )
                )
            }
        }
        return fvgs
    }

    private fun getCurrentKillzone(): KillzoneType {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in KillzoneType.LONDON.startUtcHour until KillzoneType.LONDON.endUtcHour -> KillzoneType.LONDON
            in KillzoneType.NEW_YORK.startUtcHour until KillzoneType.NEW_YORK.endUtcHour -> KillzoneType.NEW_YORK
            in KillzoneType.ASIAN.startUtcHour until KillzoneType.ASIAN.endUtcHour -> KillzoneType.ASIAN
            else -> KillzoneType.OFF_SESSION
        }
    }
}
