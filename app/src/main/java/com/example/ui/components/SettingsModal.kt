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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.websocket.DerivAuthResult
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsModal(
    initialToken: String,
    initialDailyLossCap: Double,
    initialRiskRewardRatio: Double,
    initialEnforceKillzones: Boolean,
    authResult: DerivAuthResult?,
    onSaveAndRecheck: (token: String, lossCap: Double, rrRatio: Double, enforceKillzones: Boolean) -> Unit,
    onResetBalance: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var token by remember { mutableStateOf(initialToken) }
    var isTokenVisible by remember { mutableStateOf(false) }
    var dailyLossCapText by remember { mutableStateOf(initialDailyLossCap.toInt().toString()) }
    var selectedRrRatio by remember { mutableStateOf(initialRiskRewardRatio) }
    var enforceKillzones by remember { mutableStateOf(initialEnforceKillzones) }
    var isChecking by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepPurpleDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("settings_modal_content")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Vault",
                    tint = AmberGold,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SECURE API VAULT & RISK CONTROLS",
                        color = CrispWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AES-Encrypted Storage • Deriv Real/Demo WebSocket",
                        color = SlateGrayText,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateGrayText)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Deriv API Token Input Box (Password Masked)
            Text(
                text = "Deriv API Token (Demo or Real)",
                color = CrispWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deriv_token_input"),
                placeholder = {
                    Text("Enter Deriv API Token (e.g. a1-X4b9L...)", color = SlateGrayText, fontSize = 12.sp)
                },
                singleLine = true,
                visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                        Icon(
                            imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle Token Visibility",
                            tint = SlateGrayText
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldGreen,
                    unfocusedBorderColor = DeepPurpleBorder,
                    focusedTextColor = CrispWhite,
                    unfocusedTextColor = CrispWhite,
                    cursorColor = EmeraldGreen
                )
            )

            // Auth Status Result Feedback
            authResult?.let { res ->
                Spacer(modifier = Modifier.height(6.dp))
                if (res.success) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, EmeraldGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Success", tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Authenticated: ${res.loginId} • Balance: $${res.balance} ${res.currency}",
                            color = EmeraldGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (res.errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CoralRed.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, CoralRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Error", tint = CoralRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = res.errorMessage,
                            color = CoralRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Daily Loss Cap ($300 Default) Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Daily Hard Loss Cap ($)",
                        color = CrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Bot instantly shuts off when daily loss reaches this cap.",
                        color = SlateGrayText,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = dailyLossCapText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) dailyLossCapText = it },
                    modifier = Modifier
                        .width(100.dp)
                        .testTag("daily_loss_cap_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldGreen,
                        unfocusedBorderColor = DeepPurpleBorder,
                        focusedTextColor = CrispWhite,
                        unfocusedTextColor = CrispWhite
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Risk-to-Reward Ratio Selector
            Text(
                text = "ICT Take-Profit Target (Risk:Reward)",
                color = CrispWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.5 to "1 : 1.5", 2.0 to "1 : 2.0 (Default)", 3.0 to "1 : 3.0").forEach { (ratio, label) ->
                    val isSelected = selectedRrRatio == ratio
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleMid,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) EmeraldGreen else DeepPurpleBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedRrRatio = ratio }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) EmeraldGreen else SlateGrayText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Session Killzones Enforce Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enforce London & NY Killzones Only",
                        color = CrispWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Restrict auto trades strictly to 07-10 & 12-15 UTC windows",
                        color = SlateGrayText,
                        fontSize = 10.sp
                    )
                }
                Switch(
                    checked = enforceKillzones,
                    onCheckedChange = { enforceKillzones = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CrispWhite,
                        checkedTrackColor = EmeraldGreen,
                        uncheckedTrackColor = DeepPurpleElevated
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save & Re-Check Connection Button
            Button(
                onClick = {
                    isChecking = true
                    val cap = dailyLossCapText.toDoubleOrNull() ?: 300.0
                    onSaveAndRecheck(token, cap, selectedRrRatio, enforceKillzones)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_recheck_connection_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldGreen,
                    contentColor = DarkGrayText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save & Re-check Connection",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Paper Account Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResetBalance() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Reset Paper Balance to $10,000.00",
                    color = SlateGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
