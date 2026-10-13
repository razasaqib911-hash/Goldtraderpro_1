package com.example.data.model

enum class AssetCategory {
    FOREX,
    CRYPTO
}

data class AssetPair(
    val symbol: String,
    val displayName: String,
    val category: AssetCategory,
    val decimals: Int,
    val pipSize: Double,
    val derivSymbol: String,
    val binanceSymbol: String?,
    val defaultPrice: Double,
    val defaultLotSize: Double
) {
    companion object {
        val ALL_PAIRS = listOf(
            // --- 10 Forex Pairs ---
            AssetPair(
                symbol = "XAU/USD",
                displayName = "Gold (Spot)",
                category = AssetCategory.FOREX,
                decimals = 2,
                pipSize = 0.01,
                derivSymbol = "frxXAUUSD",
                binanceSymbol = null,
                defaultPrice = 4172.50,
                defaultLotSize = 0.10
            ),
            AssetPair(
                symbol = "EUR/USD",
                displayName = "Euro / US Dollar",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxEURUSD",
                binanceSymbol = null,
                defaultPrice = 1.08520,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "GBP/USD",
                displayName = "British Pound / USD",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxGBPUSD",
                binanceSymbol = null,
                defaultPrice = 1.29840,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "USD/JPY",
                displayName = "US Dollar / Yen",
                category = AssetCategory.FOREX,
                decimals = 3,
                pipSize = 0.01,
                derivSymbol = "frxUSDJPY",
                binanceSymbol = null,
                defaultPrice = 152.420,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "AUD/USD",
                displayName = "Aussie / US Dollar",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxAUDUSD",
                binanceSymbol = null,
                defaultPrice = 0.65800,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "USD/CAD",
                displayName = "US Dollar / CAD",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxUSDCAD",
                binanceSymbol = null,
                defaultPrice = 1.38900,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "USD/CHF",
                displayName = "US Dollar / Swiss Franc",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxUSDCHF",
                binanceSymbol = null,
                defaultPrice = 0.86750,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "NZD/USD",
                displayName = "Kiwi / US Dollar",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxNZDUSD",
                binanceSymbol = null,
                defaultPrice = 0.59850,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "EUR/GBP",
                displayName = "Euro / British Pound",
                category = AssetCategory.FOREX,
                decimals = 5,
                pipSize = 0.0001,
                derivSymbol = "frxEURGBP",
                binanceSymbol = null,
                defaultPrice = 0.83580,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "GBP/JPY",
                displayName = "Pound / Japanese Yen",
                category = AssetCategory.FOREX,
                decimals = 3,
                pipSize = 0.01,
                derivSymbol = "frxGBPJPY",
                binanceSymbol = null,
                defaultPrice = 197.800,
                defaultLotSize = 0.50
            ),

            // --- 10 Crypto Pairs ---
            AssetPair(
                symbol = "BTC/USD",
                displayName = "Bitcoin",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 1.0,
                derivSymbol = "cryBTCUSD",
                binanceSymbol = "btcusdt",
                defaultPrice = 67850.00,
                defaultLotSize = 0.05
            ),
            AssetPair(
                symbol = "ETH/USD",
                displayName = "Ethereum",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 0.1,
                derivSymbol = "cryETHUSD",
                binanceSymbol = "ethusdt",
                defaultPrice = 2540.20,
                defaultLotSize = 0.20
            ),
            AssetPair(
                symbol = "SOL/USD",
                displayName = "Solana",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 0.01,
                derivSymbol = "crySOLUSD",
                binanceSymbol = "solusdt",
                defaultPrice = 175.40,
                defaultLotSize = 1.00
            ),
            AssetPair(
                symbol = "BNB/USD",
                displayName = "BNB Coin",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 0.1,
                derivSymbol = "cryBNBUSD",
                binanceSymbol = "bnbusdt",
                defaultPrice = 592.80,
                defaultLotSize = 0.50
            ),
            AssetPair(
                symbol = "XRP/USD",
                displayName = "XRP",
                category = AssetCategory.CRYPTO,
                decimals = 4,
                pipSize = 0.0001,
                derivSymbol = "cryXRPUSD",
                binanceSymbol = "xrpusdt",
                defaultPrice = 0.5240,
                defaultLotSize = 100.00
            ),
            AssetPair(
                symbol = "ADA/USD",
                displayName = "Cardano",
                category = AssetCategory.CRYPTO,
                decimals = 4,
                pipSize = 0.0001,
                derivSymbol = "cryADAUSD",
                binanceSymbol = "adausdt",
                defaultPrice = 0.3540,
                defaultLotSize = 100.00
            ),
            AssetPair(
                symbol = "DOGE/USD",
                displayName = "Dogecoin",
                category = AssetCategory.CRYPTO,
                decimals = 5,
                pipSize = 0.00001,
                derivSymbol = "cryDOGEUSD",
                binanceSymbol = "dogeusdt",
                defaultPrice = 0.14250,
                defaultLotSize = 500.00
            ),
            AssetPair(
                symbol = "AVAX/USD",
                displayName = "Avalanche",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 0.01,
                derivSymbol = "cryAVAXUSD",
                binanceSymbol = "avaxusdt",
                defaultPrice = 28.60,
                defaultLotSize = 2.00
            ),
            AssetPair(
                symbol = "LINK/USD",
                displayName = "Chainlink",
                category = AssetCategory.CRYPTO,
                decimals = 3,
                pipSize = 0.001,
                derivSymbol = "cryLINKUSD",
                binanceSymbol = "linkusdt",
                defaultPrice = 11.850,
                defaultLotSize = 5.00
            ),
            AssetPair(
                symbol = "LTC/USD",
                displayName = "Litecoin",
                category = AssetCategory.CRYPTO,
                decimals = 2,
                pipSize = 0.01,
                derivSymbol = "cryLTCUSD",
                binanceSymbol = "ltcusdt",
                defaultPrice = 71.40,
                defaultLotSize = 1.00
            )
        )

        fun findBySymbol(symbol: String): AssetPair =
            ALL_PAIRS.firstOrNull { it.symbol == symbol } ?: ALL_PAIRS.first()
    }
}
