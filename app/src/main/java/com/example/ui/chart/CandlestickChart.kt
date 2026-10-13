package com.example.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetPair
import com.example.data.model.Candle
import com.example.data.model.DealingRange
import com.example.data.model.Direction
import com.example.data.model.FairValueGap
import com.example.data.model.LiquiditySweep
import com.example.data.model.PositionStatus
import com.example.data.model.TradePosition
import com.example.data.model.TradeType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    candles: List<Candle>,
    currentPrice: Double,
    pair: AssetPair,
    dealingRange: DealingRange?,
    fvgs: List<FairValueGap>,
    latestSweep: LiquiditySweep?,
    openPositions: List<TradePosition>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Chart viewport state: pan offset & zoom
    var candleWidth by remember { mutableFloatStateOf(16f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var touchCrosshairIndex by remember { mutableStateOf<Int?>(null) }
    var touchCrosshairY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(DeepPurpleDark)
            .testTag("candlestick_chart_container")
    ) {
        if (candles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading ${pair.symbol} Chart Stream...",
                    color = SlateGrayText,
                    fontSize = 13.sp
                )
            }
        } else {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(candles.size) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            // Pinch zoom
                            candleWidth = (candleWidth * zoom).coerceIn(6f, 40f)
                            // Pan drag
                            panOffsetX = (panOffsetX + pan.x).coerceAtMost(200f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchCrosshairY = offset.y
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                touchCrosshairY = change.position.y
                                panOffsetX = (panOffsetX + dragAmount.x).coerceAtMost(200f)
                            },
                            onDragEnd = {
                                touchCrosshairIndex = null
                            }
                        )
                    }
            ) {
                val width = size.width
                val height = size.height
                val priceAxisWidth = 62.dp.toPx()
                val chartAreaWidth = width - priceAxisWidth

                val candleSpacing = candleWidth * 0.35f
                val candlePitch = candleWidth + candleSpacing

                // Calculate visible candle range from the right
                val maxVisibleCandles = (chartAreaWidth / candlePitch).toInt() + 2
                val rightMargin = 20f + panOffsetX
                val lastCandleX = chartAreaWidth - rightMargin

                // Map candle indices
                val visibleEndIndex = candles.size - 1
                val offsetCandles = (-panOffsetX / candlePitch).toInt()
                val effectiveEndIndex = (visibleEndIndex - offsetCandles).coerceIn(0, candles.size - 1)
                val effectiveStartIndex = max(0, effectiveEndIndex - maxVisibleCandles)

                val visibleCandles = candles.subList(effectiveStartIndex, effectiveEndIndex + 1)
                if (visibleCandles.isEmpty()) return@Canvas

                val minPrice = visibleCandles.minOf { it.low }
                val maxPrice = visibleCandles.maxOf { it.high }
                val priceRange = (maxPrice - minPrice).coerceAtLeast(pair.pipSize * 5)
                val paddingPrice = priceRange * 0.08
                val plotMin = minPrice - paddingPrice
                val plotMax = maxPrice + paddingPrice
                val plotRange = plotMax - plotMin

                fun priceToY(price: Double): Float {
                    val normalized = (plotMax - price) / plotRange
                    return (normalized * height).toFloat().coerceIn(0f, height)
                }

                fun yToPrice(y: Float): Double {
                    val normalized = y / height
                    return plotMax - (normalized * plotRange)
                }

                // 1. Draw horizontal grid lines & Price Axis
                val gridLines = 5
                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                for (i in 0..gridLines) {
                    val y = height * (i.toFloat() / gridLines)
                    val priceVal = yToPrice(y)

                    drawLine(
                        color = DeepPurpleBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(chartAreaWidth, y),
                        strokeWidth = 1f,
                        pathEffect = dashedEffect
                    )

                    // Price text on right margin
                    val priceStr = String.format(Locale.US, "%.${pair.decimals}f", priceVal)
                    drawText(
                        textMeasurer = textMeasurer,
                        text = priceStr,
                        topLeft = Offset(chartAreaWidth + 4.dp.toPx(), y - 7.sp.toPx()),
                        style = TextStyle(
                            color = SlateGrayText,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                // 2. Draw ICT Overlays: Dealing Range 50% Equilibrium
                dealingRange?.let { range ->
                    val eqY = priceToY(range.equilibrium)
                    drawLine(
                        color = AmberGold.copy(alpha = 0.7f),
                        start = Offset(0f, eqY),
                        end = Offset(chartAreaWidth, eqY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), 0f)
                    )
                    drawText(
                        textMeasurer = textMeasurer,
                        text = "50% EQ",
                        topLeft = Offset(8.dp.toPx(), eqY - 14.sp.toPx()),
                        style = TextStyle(color = AmberGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    )
                }

                // 3. Draw ICT Overlays: Fair Value Gaps (FVG Zones)
                fvgs.takeLast(4).forEach { fvg ->
                    val topY = priceToY(fvg.top)
                    val botY = priceToY(fvg.bottom)
                    val zoneColor = if (fvg.direction == Direction.BULLISH) {
                        EmeraldGreen.copy(alpha = 0.15f)
                    } else {
                        CoralRed.copy(alpha = 0.15f)
                    }
                    val borderColor = if (fvg.direction == Direction.BULLISH) {
                        EmeraldGreen.copy(alpha = 0.5f)
                    } else {
                        CoralRed.copy(alpha = 0.5f)
                    }

                    val zoneTop = min(topY, botY)
                    val zoneHeight = max(abs(botY - topY), 3f)

                    drawRect(
                        color = zoneColor,
                        topLeft = Offset(0f, zoneTop),
                        size = Size(chartAreaWidth, zoneHeight)
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, zoneTop),
                        end = Offset(chartAreaWidth, zoneTop),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, zoneTop + zoneHeight),
                        end = Offset(chartAreaWidth, zoneTop + zoneHeight),
                        strokeWidth = 1f
                    )

                    // Midpoint CE (50%)
                    val ceY = priceToY(fvg.midpointCe)
                    drawLine(
                        color = borderColor.copy(alpha = 0.4f),
                        start = Offset(0f, ceY),
                        end = Offset(chartAreaWidth, ceY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "${fvg.direction.name.take(4)} FVG",
                        topLeft = Offset(chartAreaWidth - 60.dp.toPx(), zoneTop + 2.dp.toPx()),
                        style = TextStyle(color = borderColor, fontSize = 8.sp, fontWeight = FontWeight.SemiBold)
                    )
                }

                // 4. Draw Candlesticks
                for (idx in effectiveStartIndex..effectiveEndIndex) {
                    val candle = candles[idx]
                    val candleIndexFromEnd = effectiveEndIndex - idx
                    val candleCenterX = lastCandleX - (candleIndexFromEnd * candlePitch)

                    if (candleCenterX < -candleWidth || candleCenterX > chartAreaWidth + candleWidth) continue

                    val openY = priceToY(candle.open)
                    val closeY = priceToY(candle.close)
                    val highY = priceToY(candle.high)
                    val lowY = priceToY(candle.low)

                    val isBullish = candle.isBullish
                    val candleColor = if (isBullish) EmeraldGreen else CoralRed

                    // Wick
                    drawLine(
                        color = candleColor,
                        start = Offset(candleCenterX, highY),
                        end = Offset(candleCenterX, lowY),
                        strokeWidth = 1.5f
                    )

                    // Body
                    val bodyTop = min(openY, closeY)
                    val bodyHeight = max(abs(openY - closeY), 2f)

                    drawRect(
                        color = candleColor,
                        topLeft = Offset(candleCenterX - candleWidth / 2f, bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )
                }

                // 5. Draw Open Trade SL & TP lines
                openPositions.filter { it.status == PositionStatus.OPEN && it.symbol == pair.symbol }.forEach { pos ->
                    val entryY = priceToY(pos.entryPrice)
                    val slY = priceToY(pos.stopLoss)
                    val tpY = priceToY(pos.takeProfit)

                    // Entry Line
                    drawLine(
                        color = CrispWhite.copy(alpha = 0.7f),
                        start = Offset(0f, entryY),
                        end = Offset(chartAreaWidth, entryY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )

                    // SL Line
                    drawLine(
                        color = CoralRed,
                        start = Offset(0f, slY),
                        end = Offset(chartAreaWidth, slY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                    )

                    // TP Line
                    drawLine(
                        color = EmeraldGreen,
                        start = Offset(0f, tpY),
                        end = Offset(chartAreaWidth, tpY),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f), 0f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "SL ${String.format(Locale.US, "%.${pair.decimals}f", pos.stopLoss)}",
                        topLeft = Offset(10.dp.toPx(), slY - 12.sp.toPx()),
                        style = TextStyle(color = CoralRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = "TP ${String.format(Locale.US, "%.${pair.decimals}f", pos.takeProfit)}",
                        topLeft = Offset(10.dp.toPx(), tpY - 12.sp.toPx()),
                        style = TextStyle(color = EmeraldGreen, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    )
                }

                // 6. Draw Current Live Price Line & Right Badge
                val currentY = priceToY(currentPrice)
                val isPriceAboveOpen = currentPrice >= (candles.lastOrNull()?.open ?: currentPrice)
                val currentPriceColor = if (isPriceAboveOpen) EmeraldGreen else CoralRed

                drawLine(
                    color = currentPriceColor,
                    start = Offset(0f, currentY),
                    end = Offset(chartAreaWidth, currentY),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Current Price Pill on Right Axis
                drawRect(
                    color = currentPriceColor,
                    topLeft = Offset(chartAreaWidth, currentY - 9.dp.toPx()),
                    size = Size(priceAxisWidth, 18.dp.toPx())
                )
                val currentPriceFormatted = String.format(Locale.US, "%.${pair.decimals}f", currentPrice)
                drawText(
                    textMeasurer = textMeasurer,
                    text = currentPriceFormatted,
                    topLeft = Offset(chartAreaWidth + 3.dp.toPx(), currentY - 6.sp.toPx()),
                    style = TextStyle(
                        color = CrispWhite,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            // Top HUD Bar overlay with candle details and Zoom +/- buttons
            val latestCandle = candles.last()
            ChartHudOverlay(
                pair = pair,
                candle = latestCandle,
                onZoomIn = { candleWidth = (candleWidth * 1.25f).coerceAtMost(40f) },
                onZoomOut = { candleWidth = (candleWidth / 1.25f).coerceAtLeast(6f) },
                onResetPan = { panOffsetX = 0f }
            )
        }
    }
}

@Composable
private fun ChartHudOverlay(
    pair: AssetPair,
    candle: Candle,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onResetPan: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val changePct = if (candle.open > 0) ((candle.close - candle.open) / candle.open) * 100 else 0.0
        val isBullish = candle.close >= candle.open
        val statusColor = if (isBullish) EmeraldGreen else CoralRed

        Row(
            modifier = Modifier
                .background(DeepPurpleElevated.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "O: ${String.format(Locale.US, "%.${pair.decimals}f", candle.open)}  " +
                        "H: ${String.format(Locale.US, "%.${pair.decimals}f", candle.high)}  " +
                        "L: ${String.format(Locale.US, "%.${pair.decimals}f", candle.low)}  " +
                        "C: ${String.format(Locale.US, "%.${pair.decimals}f", candle.close)}",
                color = SlateGrayText,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${if (changePct >= 0) "+" else ""}${String.format(Locale.US, "%.2f", changePct)}%",
                color = statusColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Zoom Controls
        Row(
            modifier = Modifier
                .background(DeepPurpleElevated.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(24.dp).testTag("chart_zoom_out")
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = CrispWhite, modifier = Modifier.size(14.dp))
            }
            IconButton(
                onClick = onResetPan,
                modifier = Modifier.size(24.dp).testTag("chart_reset_pan")
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset Pan", tint = AmberGold, modifier = Modifier.size(13.dp))
            }
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(24.dp).testTag("chart_zoom_in")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = CrispWhite, modifier = Modifier.size(14.dp))
            }
        }
    }
}

private fun abs(value: Float): Float = kotlin.math.abs(value)
