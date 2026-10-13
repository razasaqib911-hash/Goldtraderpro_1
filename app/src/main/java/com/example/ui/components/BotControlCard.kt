package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountState
import com.example.data.model.IctPipelineStatus
import com.example.data.model.TradePosition
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.SlateGrayText
import java.util.Locale
import kotlin.math.abs

@Composable
fun BotControlCard(
    accountState: AccountState,
    pipelineStatus: IctPipelineStatus,
    openPositions: List<TradePosition>,
    onToggleAutoMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalDailyLoss = abs(minOf(0.0, accountState.totalDailyPnL(openPositions)))
    val dailyCap = accountState.dailyLossCap
    val isCapBreached = accountState.isDailyCapLocked || totalDailyLoss >= dailyCap
    val capProgress = (totalDailyLoss / dailyCap).coerceIn(0.0, 1.0).toFloat()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .background(DeepPurpleMid, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (isCapBreached) CoralRed else if (accountState.autoTradingEnabled) EmeraldGreen else DeepPurpleBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Header Row: Bot Title & Prominent Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (accountState.autoTradingEnabled) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleElevated,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (accountState.autoTradingEnabled) Icons.Default.AutoAwesome else Icons.Default.PowerSettingsNew,
                        contentDescription = "Auto Bot",
                        tint = if (accountState.autoTradingEnabled) EmeraldGreen else SlateGrayText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ICT AUTOMATION ENGINE",
                            color = CrispWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (accountState.autoTradingEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(EmeraldGreen, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = DeepPurpleDark,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isCapBreached) "DAILY LOSS CAP HIT - BOT LOCKED"
                        else if (accountState.autoTradingEnabled) "Active: Continuous Scanning & Auto Entry"
                        else "Bot Standby: Toggle to activate ICT pipeline",
                        color = if (isCapBreached) CoralRed else if (accountState.autoTradingEnabled) EmeraldGreen else SlateGrayText,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Prominent Auto Mode Toggle
                Switch(
                    checked = accountState.autoTradingEnabled && !isCapBreached,
                    onCheckedChange = { onToggleAutoMode(it) },
                    enabled = !isCapBreached,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CrispWhite,
                        checkedTrackColor = EmeraldGreen,
                        uncheckedThumbColor = SlateGrayText,
                        uncheckedTrackColor = DeepPurpleElevated,
                        disabledCheckedTrackColor = CoralRed.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.testTag("auto_mode_toggle")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pipeline Step Progress Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepPurpleDark, RoundedCornerShape(8.dp))
                    .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pipelineStatus.title,
                            color = if (pipelineStatus.isSignalActive) EmeraldGreen else AmberGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Step ${pipelineStatus.step}/6",
                            color = SlateGrayText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = pipelineStatus.detail,
                        color = CrispWhite,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // 6-Step Visualizer dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (stepIdx in 1..6) {
                            val isCompleted = stepIdx <= pipelineStatus.step
                            val isCurrent = stepIdx == pipelineStatus.step
                            val stepColor = if (isCompleted) EmeraldGreen else DeepPurpleBorder
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .padding(horizontal = 2.dp)
                                    .background(stepColor, RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Loss Cap ($300 Default) Hard Stop Limit Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isCapBreached) CoralRed.copy(alpha = 0.15f) else DeepPurpleElevated,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (isCapBreached) CoralRed else DeepPurpleBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCapBreached) Icons.Default.Lock else Icons.Default.Warning,
                            contentDescription = "Safety Vault",
                            tint = if (isCapBreached) CoralRed else AmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Safety Vault Daily Cap ($300)",
                            color = CrispWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "-$${String.format(Locale.US, "%.2f", totalDailyLoss)} / $${String.format(Locale.US, "%.0f", dailyCap)}",
                            color = if (isCapBreached) CoralRed else SlateGrayText,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { capProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = if (capProgress > 0.8f) CoralRed else AmberGold,
                        trackColor = DeepPurpleDark
                    )
                }
            }
        }
    }
}
