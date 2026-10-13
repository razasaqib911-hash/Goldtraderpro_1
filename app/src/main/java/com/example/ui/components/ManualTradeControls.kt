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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetPair
import com.example.data.model.TradeType
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CoralRedDark
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkGrayText
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.SlateGrayText
import java.util.Locale

@Composable
fun ManualTradeControls(
    pair: AssetPair,
    currentPrice: Double,
    lotSize: Double,
    isCapBreached: Boolean,
    onAdjustLotSize: (Double) -> Unit,
    onExecuteManualTrade: (TradeType) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(DeepPurpleMid, RoundedCornerShape(12.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            // Header: Stake / Lot Size Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manual Override Execution",
                    color = CrispWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                // Lot size controls
                Row(
                    modifier = Modifier
                        .background(DeepPurpleDark, RoundedCornerShape(8.dp))
                        .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val step = if (pair.defaultLotSize >= 1.0) 1.0 else 0.05
                            val newLot = (lotSize - step).coerceAtLeast(step)
                            onAdjustLotSize(newLot)
                        },
                        modifier = Modifier.size(24.dp).testTag("stake_decrease")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Stake", tint = CrispWhite, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "Lot: ${String.format(Locale.US, "%.2f", lotSize)}",
                        color = CrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = {
                            val step = if (pair.defaultLotSize >= 1.0) 1.0 else 0.05
                            val newLot = (lotSize + step).coerceAtMost(50.0)
                            onAdjustLotSize(newLot)
                        },
                        modifier = Modifier.size(24.dp).testTag("stake_increase")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Stake", tint = CrispWhite, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual Buy & Sell Execution Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // BUY Button
                Button(
                    onClick = { onExecuteManualTrade(TradeType.BUY) },
                    enabled = !isCapBreached,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = DarkGrayText,
                        disabledContainerColor = EmeraldGreen.copy(alpha = 0.3f),
                        disabledContentColor = CrispWhite.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("manual_buy_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Buy",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "BUY / LONG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = String.format(Locale.US, "%.${pair.decimals}f", currentPrice),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // SELL Button
                Button(
                    onClick = { onExecuteManualTrade(TradeType.SELL) },
                    enabled = !isCapBreached,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CoralRed,
                        contentColor = CrispWhite,
                        disabledContainerColor = CoralRed.copy(alpha = 0.3f),
                        disabledContentColor = CrispWhite.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("manual_sell_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Sell",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "SELL / SHORT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = String.format(Locale.US, "%.${pair.decimals}f", currentPrice),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
