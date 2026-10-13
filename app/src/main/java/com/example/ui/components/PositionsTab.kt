package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PositionStatus
import com.example.data.model.TradePosition
import com.example.data.model.TradeType
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PositionsTab(
    positions: List<TradePosition>,
    tradeHistory: List<TradePosition>,
    onClosePosition: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistory by remember { mutableStateOf(false) }

    val openPositions = positions.filter { it.status == PositionStatus.OPEN }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(DeepPurpleMid, RoundedCornerShape(12.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            // Tab Header: Active Positions vs History
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .background(DeepPurpleDark, RoundedCornerShape(8.dp))
                        .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (!showHistory) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleDark,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { showHistory = false }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("tab_active_positions")
                    ) {
                        Text(
                            text = "Open (${openPositions.size})",
                            color = if (!showHistory) EmeraldGreen else SlateGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                if (showHistory) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleDark,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { showHistory = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("tab_trade_history")
                    ) {
                        Text(
                            text = "History (${tradeHistory.size})",
                            color = if (showHistory) EmeraldGreen else SlateGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!showHistory && openPositions.isNotEmpty()) {
                    val totalUnrealized = openPositions.sumOf { it.unrealizedPnL }
                    Text(
                        text = "Float: ${if (totalUnrealized >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", totalUnrealized)}",
                        color = if (totalUnrealized >= 0) EmeraldGreen else CoralRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!showHistory) {
                // Open Positions List
                if (openPositions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "No Open Positions",
                                tint = SlateGrayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No active open trades. Bot is scanning.",
                                color = SlateGrayText,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        openPositions.forEach { pos ->
                            OpenPositionRow(pos = pos, onClose = { onClosePosition(pos.id) })
                        }
                    }
                }
            } else {
                // Closed Trades History List
                if (tradeHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "No History",
                                tint = SlateGrayText.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "No closed trades recorded yet.",
                                color = SlateGrayText,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tradeHistory.takeLast(10).reversed().forEach { pos ->
                            HistoryRow(pos = pos)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenPositionRow(
    pos: TradePosition,
    onClose: () -> Unit
) {
    val pnl = pos.unrealizedPnL
    val pnlColor = if (pnl >= 0) EmeraldGreen else CoralRed

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepPurpleDark, RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
            .testTag("position_row_${pos.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (pos.type == TradeType.BUY) EmeraldGreen.copy(alpha = 0.2f) else CoralRed.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pos.type.name,
                            color = if (pos.type == TradeType.BUY) EmeraldGreen else CoralRed,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${pos.symbol} (${pos.lotSize})",
                        color = CrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Entry: ${String.format(Locale.US, "%.4f", pos.entryPrice)} | SL: ${String.format(Locale.US, "%.4f", pos.stopLoss)} | TP: ${String.format(Locale.US, "%.4f", pos.takeProfit)}",
                    color = SlateGrayText,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (pnl >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", pnl)}",
                    color = pnlColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(2.dp))

                Box(
                    modifier = Modifier
                        .background(CoralRed.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .border(1.dp, CoralRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .clickable { onClose() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("close_pos_btn_${pos.id}")
                ) {
                    Text(
                        text = "Close",
                        color = CoralRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    pos: TradePosition
) {
    val pnl = pos.realizedPnL
    val pnlColor = if (pnl >= 0) EmeraldGreen else CoralRed
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }
    val timeStr = remember(pos.closeTime) {
        dateFormat.format(Date(pos.closeTime ?: pos.openTime))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepPurpleDark, RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${pos.type.name} ${pos.symbol}",
                        color = CrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pos.status.name.replace("CLOSED_", ""),
                        color = when (pos.status) {
                            PositionStatus.CLOSED_TP -> EmeraldGreen
                            PositionStatus.CLOSED_SL -> CoralRed
                            else -> AmberGold
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "$timeStr • ${pos.comment}",
                    color = SlateGrayText,
                    fontSize = 9.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "${if (pnl >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", pnl)}",
                color = pnlColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
