package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.chart.CandlestickChart
import com.example.ui.components.AssetSelectorBar
import com.example.ui.components.BotControlCard
import com.example.ui.components.IctRadarCard
import com.example.ui.components.ManualTradeControls
import com.example.ui.components.PositionsTab
import com.example.ui.components.SettingsModal
import com.example.ui.components.TimeframeSelector
import com.example.ui.components.TopBar
import com.example.ui.theme.DeepPurpleDark

@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("main_screen_scaffold"),
        containerColor = DeepPurpleDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Top Navigation & Status Bar
            TopBar(
                accountState = uiState.accountState,
                openPositions = uiState.openPositions,
                onOpenSettings = { viewModel.setSettingsModalVisible(true) }
            )

            // 2. Asset Selector Tabs (10 Forex + 10 Crypto)
            AssetSelectorBar(
                currentPair = uiState.currentPair,
                currentPrice = uiState.currentPrice,
                onSelectPair = { viewModel.selectPair(it) }
            )

            // 3. Multi-Timeframe Selector (M1 - W1)
            TimeframeSelector(
                selectedTimeframe = uiState.selectedTimeframe,
                onSelectTimeframe = { viewModel.selectTimeframe(it) }
            )

            // 4. Interactive Movable Candlestick Chart with ICT Overlays
            CandlestickChart(
                candles = uiState.candles,
                currentPrice = uiState.currentPrice,
                pair = uiState.currentPair,
                dealingRange = uiState.pipelineStatus?.dealingRange,
                fvgs = uiState.fvgs,
                latestSweep = uiState.pipelineStatus?.latestSweep,
                openPositions = uiState.openPositions
            )

            // 5. ICT Automation Bot Control Switch & Daily Loss Cap ($300 Default)
            uiState.pipelineStatus?.let { status ->
                BotControlCard(
                    accountState = uiState.accountState,
                    pipelineStatus = status,
                    openPositions = uiState.openPositions,
                    onToggleAutoMode = { viewModel.toggleAutoTrading(it) }
                )
            }

            // 6. Manual Override Buy / Sell Execution Controls
            ManualTradeControls(
                pair = uiState.currentPair,
                currentPrice = uiState.currentPrice,
                lotSize = uiState.currentLotSize,
                isCapBreached = uiState.accountState.isDailyCapLocked,
                onAdjustLotSize = { viewModel.adjustLotSize(it) },
                onExecuteManualTrade = { viewModel.executeManualTrade(it) }
            )

            // 7. ICT Model Metrics & IPDA Radar
            uiState.pipelineStatus?.let { status ->
                IctRadarCard(
                    pipelineStatus = status,
                    fvgs = uiState.fvgs
                )
            }

            // 8. Open Positions & Trade History Ledger
            PositionsTab(
                positions = uiState.openPositions,
                tradeHistory = uiState.tradeHistory,
                onClosePosition = { viewModel.closePosition(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dedicated Settings & API Vault Bottom Sheet
        if (uiState.showSettingsModal) {
            SettingsModal(
                initialToken = uiState.accountState.derivToken,
                initialDailyLossCap = uiState.accountState.dailyLossCap,
                initialRiskRewardRatio = 2.0,
                initialEnforceKillzones = true,
                authResult = uiState.authResult,
                onSaveAndRecheck = { token, cap, rrRatio, enforceKz ->
                    viewModel.saveVaultSettings(token, cap, rrRatio, enforceKz)
                },
                onResetBalance = { viewModel.resetPaperBalance() },
                onDismiss = { viewModel.setSettingsModalVisible(false) }
            )
        }
    }
}
