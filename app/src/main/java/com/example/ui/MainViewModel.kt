package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.IctTradingEngine
import com.example.data.model.AccountState
import com.example.data.model.AssetCategory
import com.example.data.model.AssetPair
import com.example.data.model.Candle
import com.example.data.model.ConnectionStatus
import com.example.data.model.Direction
import com.example.data.model.FairValueGap
import com.example.data.model.IctPipelineStatus
import com.example.data.model.PositionStatus
import com.example.data.model.Timeframe
import com.example.data.model.TradePosition
import com.example.data.model.TradeType
import com.example.data.vault.SecureVaultStorage
import com.example.data.websocket.DerivAuthResult
import com.example.data.websocket.FeedManager
import com.example.data.websocket.LiveTick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.random.Random

data class MainUiState(
    val currentPair: AssetPair = AssetPair.ALL_PAIRS.first(), // XAU/USD Gold
    val selectedTimeframe: Timeframe = Timeframe.M5,
    val currentPrice: Double = AssetPair.ALL_PAIRS.first().defaultPrice,
    val candles: List<Candle> = emptyList(),
    val fvgs: List<FairValueGap> = emptyList(),
    val pipelineStatus: IctPipelineStatus? = null,
    val openPositions: List<TradePosition> = emptyList(),
    val tradeHistory: List<TradePosition> = emptyList(),
    val accountState: AccountState = AccountState(),
    val currentLotSize: Double = AssetPair.ALL_PAIRS.first().defaultLotSize,
    val showSettingsModal: Boolean = false,
    val authResult: DerivAuthResult? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val vaultStorage = SecureVaultStorage(application.applicationContext)
    private val feedManager = FeedManager(viewModelScope)
    private val ictEngine = IctTradingEngine()

    private val _uiState = MutableStateFlow(
        MainUiState(
            accountState = AccountState(
                dailyLossCap = vaultStorage.getDailyLossCap(),
                derivToken = vaultStorage.getDerivToken()
            )
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var activeRiskRewardRatio: Double = vaultStorage.getRiskRewardRatio()
    private var enforceKillzones: Boolean = vaultStorage.getEnforceKillzones()

    init {
        // Load stored token and initiate connection
        val savedToken = vaultStorage.getDerivToken()
        if (savedToken.isNotBlank()) {
            feedManager.setDerivToken(savedToken)
        }

        // Initialize historical candles for default pair
        generateInitialCandleHistory(_uiState.value.currentPair, _uiState.value.selectedTimeframe)

        // Listen for live ticks
        viewModelScope.launch(Dispatchers.Default) {
            feedManager.tickFlow.collectLatest { tick ->
                processTick(tick)
            }
        }

        // Listen for connection status
        viewModelScope.launch {
            feedManager.connectionStatus.collectLatest { status ->
                _uiState.value = _uiState.value.copy(
                    accountState = _uiState.value.accountState.copy(connectionStatus = status)
                )
            }
        }

        // Listen for Deriv auth
        viewModelScope.launch {
            feedManager.derivAuthResult.collectLatest { auth ->
                auth?.let {
                    _uiState.value = _uiState.value.copy(
                        authResult = it,
                        accountState = if (it.success) {
                            _uiState.value.accountState.copy(
                                isDerivAuthorized = true,
                                balance = it.balance,
                                derivCurrency = it.currency,
                                derivAccountId = it.loginId
                            )
                        } else {
                            _uiState.value.accountState
                        }
                    )
                }
            }
        }
    }

    private fun processTick(tick: LiveTick) {
        val state = _uiState.value
        if (tick.symbol != state.currentPair.symbol) return

        val newPrice = tick.price
        val currentCandles = state.candles.toMutableList()

        if (currentCandles.isNotEmpty()) {
            val last = currentCandles.last()
            val tfSeconds = state.selectedTimeframe.seconds
            val nowSec = tick.timestamp / 1000

            // If candle expired, push a new candle, else update high, low, close
            if (nowSec - last.timestamp >= tfSeconds) {
                val newCandle = Candle(
                    timestamp = nowSec,
                    open = last.close,
                    high = maxOf(last.close, newPrice),
                    low = minOf(last.close, newPrice),
                    close = newPrice,
                    volume = 1.0
                )
                currentCandles.add(newCandle)
                if (currentCandles.size > 80) currentCandles.removeAt(0)
            } else {
                val updated = last.copy(
                    high = maxOf(last.high, newPrice),
                    low = minOf(last.low, newPrice),
                    close = newPrice,
                    volume = last.volume + 1.0
                )
                currentCandles[currentCandles.size - 1] = updated
            }
        }

        // Run ICT Pipeline Analysis
        val (pipelineStatus, trigger) = ictEngine.analyze(
            candles = currentCandles,
            currentPrice = newPrice,
            riskRewardRatio = activeRiskRewardRatio,
            enforceKillzones = enforceKillzones
        )
        val fvgs = ictEngine.detectFvgs(currentCandles)

        // Update Open Positions (Check SL and TP)
        val updatedPositions = mutableListOf<TradePosition>()
        val closedPositions = mutableListOf<TradePosition>()
        var newRealizedPnL = state.accountState.realizedDailyPnL

        for (pos in state.openPositions) {
            if (pos.status != PositionStatus.OPEN) continue

            val updatedPos = pos.copy(currentPrice = newPrice)

            // Check TP or SL hit
            var isClosed = false
            var closeStatus = PositionStatus.OPEN
            var comment = ""

            if (pos.type == TradeType.BUY) {
                if (newPrice >= pos.takeProfit) {
                    isClosed = true
                    closeStatus = PositionStatus.CLOSED_TP
                    comment = "Take Profit Hit (+1:${String.format("%.1f", activeRiskRewardRatio)})"
                } else if (newPrice <= pos.stopLoss) {
                    isClosed = true
                    closeStatus = PositionStatus.CLOSED_SL
                    comment = "Stop Loss Hit"
                }
            } else {
                if (newPrice <= pos.takeProfit) {
                    isClosed = true
                    closeStatus = PositionStatus.CLOSED_TP
                    comment = "Take Profit Hit (+1:${String.format("%.1f", activeRiskRewardRatio)})"
                } else if (newPrice >= pos.stopLoss) {
                    isClosed = true
                    closeStatus = PositionStatus.CLOSED_SL
                    comment = "Stop Loss Hit"
                }
            }

            if (isClosed) {
                val finalPnL = updatedPos.unrealizedPnL
                newRealizedPnL += finalPnL
                val closed = updatedPos.copy(
                    status = closeStatus,
                    closePrice = newPrice,
                    closeTime = System.currentTimeMillis(),
                    realizedPnL = finalPnL,
                    comment = comment
                )
                closedPositions.add(closed)
            } else {
                updatedPositions.add(updatedPos)
            }
        }

        // Check Daily Hard Loss Cap ($300 Default)
        val totalDailyLoss = abs(minOf(0.0, newRealizedPnL + updatedPositions.sumOf { it.unrealizedPnL }))
        val isDailyCapLocked = totalDailyLoss >= state.accountState.dailyLossCap
        val autoEnabled = state.accountState.autoTradingEnabled && !isDailyCapLocked

        // If auto trading is active, cap is safe, and trigger fires, execute auto trade
        if (autoEnabled && trigger != null && updatedPositions.none { it.symbol == state.currentPair.symbol }) {
            val autoPos = TradePosition(
                id = UUID.randomUUID().toString().take(8),
                symbol = state.currentPair.symbol,
                type = trigger.type,
                entryPrice = trigger.entryPrice,
                currentPrice = trigger.entryPrice,
                stopLoss = trigger.stopLoss,
                takeProfit = trigger.takeProfit,
                lotSize = state.currentLotSize,
                comment = trigger.reason
            )
            updatedPositions.add(autoPos)
        }

        val newHistory = if (closedPositions.isNotEmpty()) {
            state.tradeHistory + closedPositions
        } else state.tradeHistory

        val newBalance = state.accountState.balance + closedPositions.sumOf { it.realizedPnL }

        _uiState.value = state.copy(
            currentPrice = newPrice,
            candles = currentCandles,
            fvgs = fvgs,
            pipelineStatus = pipelineStatus,
            openPositions = updatedPositions,
            tradeHistory = newHistory,
            accountState = state.accountState.copy(
                balance = newBalance,
                realizedDailyPnL = newRealizedPnL,
                isDailyCapLocked = isDailyCapLocked,
                autoTradingEnabled = autoEnabled,
                activeWebSocketFeed = tick.source
            )
        )
    }

    fun selectPair(pair: AssetPair) {
        if (pair.symbol == _uiState.value.currentPair.symbol) return
        _uiState.value = _uiState.value.copy(
            currentPair = pair,
            currentPrice = pair.defaultPrice,
            currentLotSize = pair.defaultLotSize
        )
        generateInitialCandleHistory(pair, _uiState.value.selectedTimeframe)
        feedManager.switchPair(pair)
    }

    fun selectTimeframe(timeframe: Timeframe) {
        if (timeframe == _uiState.value.selectedTimeframe) return
        _uiState.value = _uiState.value.copy(selectedTimeframe = timeframe)
        generateInitialCandleHistory(_uiState.value.currentPair, timeframe)
    }

    fun toggleAutoTrading(enabled: Boolean) {
        if (_uiState.value.accountState.isDailyCapLocked && enabled) return
        _uiState.value = _uiState.value.copy(
            accountState = _uiState.value.accountState.copy(autoTradingEnabled = enabled)
        )
    }

    fun adjustLotSize(newLot: Double) {
        _uiState.value = _uiState.value.copy(currentLotSize = newLot)
    }

    fun executeManualTrade(type: TradeType) {
        val state = _uiState.value
        if (state.accountState.isDailyCapLocked) return

        val price = state.currentPrice
        val pair = state.currentPair
        val slDistance = when {
            pair.symbol == "XAU/USD" -> 6.50
            pair.category == AssetCategory.CRYPTO -> pair.defaultPrice * 0.015
            else -> pair.pipSize * 25.0
        }
        val tpDistance = slDistance * activeRiskRewardRatio

        val sl = if (type == TradeType.BUY) price - slDistance else price + slDistance
        val tp = if (type == TradeType.BUY) price + tpDistance else price - tpDistance

        val newPos = TradePosition(
            id = UUID.randomUUID().toString().take(8),
            symbol = pair.symbol,
            type = type,
            entryPrice = price,
            currentPrice = price,
            stopLoss = sl,
            takeProfit = tp,
            lotSize = state.currentLotSize,
            comment = "Manual Override Entry"
        )

        _uiState.value = state.copy(
            openPositions = state.openPositions + newPos
        )
    }

    fun closePosition(positionId: String) {
        val state = _uiState.value
        val pos = state.openPositions.firstOrNull { it.id == positionId } ?: return

        val closed = pos.copy(
            status = PositionStatus.CLOSED_MANUAL,
            closePrice = state.currentPrice,
            closeTime = System.currentTimeMillis(),
            realizedPnL = pos.unrealizedPnL,
            comment = "Manual Close"
        )

        _uiState.value = state.copy(
            openPositions = state.openPositions.filter { it.id != positionId },
            tradeHistory = state.tradeHistory + closed,
            accountState = state.accountState.copy(
                balance = state.accountState.balance + closed.realizedPnL,
                realizedDailyPnL = state.accountState.realizedDailyPnL + closed.realizedPnL
            )
        )
    }

    fun setSettingsModalVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(showSettingsModal = visible)
    }

    fun saveVaultSettings(token: String, dailyLossCap: Double, rrRatio: Double, enforceKz: Boolean) {
        vaultStorage.saveDerivToken(token)
        vaultStorage.saveDailyLossCap(dailyLossCap)
        vaultStorage.saveRiskRewardRatio(rrRatio)
        vaultStorage.saveEnforceKillzones(enforceKz)

        activeRiskRewardRatio = rrRatio
        enforceKillzones = enforceKz

        _uiState.value = _uiState.value.copy(
            accountState = _uiState.value.accountState.copy(
                dailyLossCap = dailyLossCap,
                derivToken = token
            )
        )

        feedManager.recheckConnection(token)
    }

    fun resetPaperBalance() {
        _uiState.value = _uiState.value.copy(
            openPositions = emptyList(),
            tradeHistory = emptyList(),
            accountState = _uiState.value.accountState.copy(
                balance = 10000.0,
                realizedDailyPnL = 0.0,
                isDailyCapLocked = false
            )
        )
    }

    /**
     * Synthesizes 60 candles with genuine ICT market structure (swings, displacement, FVG imbalances)
     */
    private fun generateInitialCandleHistory(pair: AssetPair, timeframe: Timeframe) {
        val count = 55
        val list = mutableListOf<Candle>()
        val basePrice = pair.defaultPrice
        val tfSec = timeframe.seconds
        val startSec = (System.currentTimeMillis() / 1000) - (count * tfSec)

        val volatility = when {
            pair.symbol == "XAU/USD" -> 4.5
            pair.category == AssetCategory.CRYPTO -> pair.pipSize * 8.0
            else -> pair.pipSize * 2.5
        }
        var currentClose = basePrice - (volatility * 12.0)

        for (i in 0 until count) {
            val ts = startSec + (i * tfSec)
            val open = currentClose
            // Create cyclical trend with swing displacements for ICT
            val trendBias = if (i in 15..28) 1.2 else if (i in 29..42) -1.2 else 0.4
            val change = ((Random.nextDouble() - 0.45) + (trendBias * 0.15)) * volatility
            val close = open + change
            val high = maxOf(open, close) + (Random.nextDouble() * volatility * 0.6)
            val low = minOf(open, close) - (Random.nextDouble() * volatility * 0.6)

            list.add(
                Candle(
                    timestamp = ts,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextDouble(50.0, 500.0)
                )
            )
            currentClose = close
        }

        val (pipelineStatus, _) = ictEngine.analyze(list, currentClose, activeRiskRewardRatio, enforceKillzones)
        val fvgs = ictEngine.detectFvgs(list)

        _uiState.value = _uiState.value.copy(
            candles = list,
            currentPrice = currentClose,
            fvgs = fvgs,
            pipelineStatus = pipelineStatus
        )
    }

    override fun onCleared() {
        super.onCleared()
        feedManager.disconnect()
    }
}
