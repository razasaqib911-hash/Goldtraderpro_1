package com.example.data.model

enum class Timeframe(
    val label: String,
    val seconds: Long,
    val derivGranularity: Int,
    val binanceInterval: String
) {
    M1("M1", 60L, 60, "1m"),
    M5("M5", 300L, 300, "5m"),
    M15("M15", 900L, 900, "15m"),
    M30("M30", 1800L, 1800, "30m"),
    H1("H1", 3600L, 3600, "1h"),
    H4("H4", 14400L, 14400, "4h"),
    DAILY("D1", 86400L, 86400, "1d"),
    WEEKLY("W1", 604800L, 604800, "1w")
}
