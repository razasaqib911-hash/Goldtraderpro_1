package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountState
import com.example.data.model.ConnectionStatus
import com.example.data.model.TradePosition
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralRed
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText
import java.util.Locale

@Composable
fun TopBar(
    accountState: AccountState,
    openPositions: List<TradePosition>,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DeepPurpleDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & WS Status Pulse
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing status dot
                    val statusDotColor = when (accountState.connectionStatus) {
                        ConnectionStatus.LIVE_WEBSOCKET -> EmeraldGreen
                        ConnectionStatus.RECONNECTING -> AmberGold
                        ConnectionStatus.OFFLINE -> CoralRed
                    }
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .alpha(if (accountState.connectionStatus == ConnectionStatus.LIVE_WEBSOCKET) pulseAlpha else 1f)
                            .background(statusDotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GoldTraderPro",
                        color = CrispWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Feed source indicator
                Text(
                    text = "${accountState.activeWebSocketFeed} • ${accountState.connectionStatus.name.replace("_", " ")}",
                    color = SlateGrayText,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Account Balance & Equity Display
            val equity = accountState.calculateEquity(openPositions)
            val dailyPnL = accountState.totalDailyPnL(openPositions)
            val pnlColor = if (dailyPnL >= 0) EmeraldGreen else CoralRed

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Account Mode Badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (accountState.isDerivAuthorized) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleElevated,
                                RoundedCornerShape(4.dp)
                            )
                            .border(1.dp, if (accountState.isDerivAuthorized) EmeraldGreen else DeepPurpleBorder, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (accountState.isDerivAuthorized) "DERIV ${accountState.derivCurrency}" else "DEMO",
                            color = if (accountState.isDerivAuthorized) EmeraldGreen else AmberGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$${String.format(Locale.US, "%,.2f", equity)}",
                        color = CrispWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Daily PnL
                Text(
                    text = "Daily: ${if (dailyPnL >= 0) "+" else ""}$${String.format(Locale.US, "%.2f", dailyPnL)}",
                    color = pnlColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Settings Gear Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepPurpleMid, RoundedCornerShape(10.dp))
                    .border(1.dp, DeepPurpleBorder, RoundedCornerShape(10.dp))
                    .testTag("top_bar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "API Vault Settings",
                    tint = CrispWhite,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
