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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
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
import com.example.data.model.Direction
import com.example.data.model.FairValueGap
import com.example.data.model.IctPipelineStatus
import com.example.data.model.RangeZone
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText

@Composable
fun IctRadarCard(
    pipelineStatus: IctPipelineStatus,
    fvgs: List<FairValueGap>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .background(DeepPurpleMid, RoundedCornerShape(12.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
            .testTag("ict_radar_card")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "ICT Analytics",
                    tint = AmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ICT Model Metrics & IPDA Radar",
                    color = CrispWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = pipelineStatus.killzone.label,
                    color = if (pipelineStatus.isKillzoneValid) EmeraldGreen else SlateGrayText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4-Quadrant Metric Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quadrant 1: HTF Bias
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "HTF Bias (IPDA)",
                    value = pipelineStatus.htfBias.name,
                    valueColor = when (pipelineStatus.htfBias) {
                        Direction.BULLISH -> EmeraldGreen
                        Direction.BEARISH -> CoralRed
                        Direction.NEUTRAL -> SlateGrayText
                    },
                    subtitle = "20/40/60 Lookback"
                )

                // Quadrant 2: Dealing Range Zone
                val zone = pipelineStatus.dealingRange?.zone ?: RangeZone.EQUILIBRIUM
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "Dealing Range",
                    value = zone.name,
                    valueColor = when (zone) {
                        RangeZone.DISCOUNT -> EmeraldGreen
                        RangeZone.PREMIUM -> CoralRed
                        RangeZone.EQUILIBRIUM -> AmberGold
                    },
                    subtitle = if (zone == RangeZone.DISCOUNT) "Valid for BUYS" else "Valid for SELLS"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quadrant 3: ERL Liquidity Sweep
                val sweep = pipelineStatus.latestSweep
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "Liquidity (ERL)",
                    value = if (sweep != null) "SWEPT" else "SCANNING",
                    valueColor = if (sweep != null) AmberGold else SlateGrayText,
                    subtitle = sweep?.label ?: "PDH / PDL Swings"
                )

                // Quadrant 4: FVG Imbalances
                val activeFvgCount = fvgs.count { !it.isFilled }
                MetricBox(
                    modifier = Modifier.weight(1f),
                    title = "FVG Imbalance",
                    value = "$activeFvgCount Active",
                    valueColor = if (activeFvgCount > 0) CyanAccent else SlateGrayText,
                    subtitle = pipelineStatus.activeFvg?.direction?.name ?: "No Open Gap"
                )
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    valueColor: Color,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(DeepPurpleDark, RoundedCornerShape(8.dp))
            .border(1.dp, DeepPurpleBorder, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = title,
                color = SlateGrayText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                color = SlateGrayText.copy(alpha = 0.7f),
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
