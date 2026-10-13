package com.example.data.model

enum class TradeType {
    BUY,
    SELL
}

enum class PositionStatus {
    OPEN,
    CLOSED_TP,
    CLOSED_SL,
    CLOSED_MANUAL,
    CLOSED_DAILY_CAP
}

data class TradePosition(
    val id: String,
    val symbol: String,
    val type: TradeType,
    val entryPrice: Double,
    val currentPrice: Double,
    val stopLoss: Double,
    val takeProfit: Double,
    val lotSize: Double,
    val openTime: Long = System.currentTimeMillis(),
    val closeTime: Long? = null,
    val status: PositionStatus = PositionStatus.OPEN,
    val closePrice: Double? = null,
    val realizedPnL: Double = 0.0,
    val comment: String = "ICT FVG Auto"
) {
    val unrealizedPnL: Double
        get() {
            if (status != PositionStatus.OPEN) return realizedPnL
            val diff = if (type == TradeType.BUY) {
                currentPrice - entryPrice
            } else {
                entryPrice - currentPrice
            }
            val pair = AssetPair.findBySymbol(symbol)
            return when {
                symbol == "XAU/USD" -> diff * (lotSize * 100.0)
                pair.category == AssetCategory.FOREX -> (diff / pair.pipSize) * 10.0 * lotSize
                else -> diff * lotSize
            }
        }
}

enum class ConnectionStatus {
    LIVE_WEBSOCKET,
    RECONNECTING,
    OFFLINE
}

data class AccountState(
    val balance: Double = 10000.00,
    val dailyLossCap: Double = 300.00,
    val realizedDailyPnL: Double = 0.00,
    val autoTradingEnabled: Boolean = false,
    val isDailyCapLocked: Boolean = false,
    val isDerivAuthorized: Boolean = false,
    val derivToken: String = "",
    val derivAccountId: String = "CR-DEMO9942",
    val derivCurrency: String = "USD",
    val connectionStatus: ConnectionStatus = ConnectionStatus.LIVE_WEBSOCKET,
    val activeWebSocketFeed: String = "Deriv / Binance WS"
) {
    fun calculateEquity(openPositions: List<TradePosition>): Double {
        val totalUnrealized = openPositions.filter { it.status == PositionStatus.OPEN }.sumOf { it.unrealizedPnL }
        return balance + totalUnrealized
    }

    fun totalDailyPnL(openPositions: List<TradePosition>): Double {
        val totalUnrealized = openPositions.filter { it.status == PositionStatus.OPEN }.sumOf { it.unrealizedPnL }
        return realizedDailyPnL + totalUnrealized
    }
}
