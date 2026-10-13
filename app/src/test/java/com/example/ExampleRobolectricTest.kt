package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.engine.IctTradingEngine
import com.example.data.model.AssetCategory
import com.example.data.model.AssetPair
import com.example.data.model.Candle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("GoldTraderPro", appName)
    }

    @Test
    fun `verify exactly 20 asset pairs with 10 forex and 10 crypto`() {
        assertEquals(20, AssetPair.ALL_PAIRS.size)
        val forexCount = AssetPair.ALL_PAIRS.count { it.category == AssetCategory.FOREX }
        val cryptoCount = AssetPair.ALL_PAIRS.count { it.category == AssetCategory.CRYPTO }
        assertEquals(10, forexCount)
        assertEquals(10, cryptoCount)

        // Verify key pairs exist
        val gold = AssetPair.findBySymbol("XAU/USD")
        assertNotNull(gold)
        assertTrue("Gold price should be in 4100-4200 range", gold.defaultPrice in 4100.0..4200.0)
        assertNotNull(AssetPair.findBySymbol("BTC/USD"))
    }

    @Test
    fun `verify ict engine pipeline execution`() {
        val engine = IctTradingEngine()
        val dummyCandles = (1..30).map { i ->
            Candle(
                timestamp = i * 60L,
                open = 4150.0 + i,
                high = 4152.0 + i,
                low = 4149.0 + i,
                close = 4151.0 + i
            )
        }
        val (status, _) = engine.analyze(dummyCandles, 4180.0)
        assertNotNull(status)
        assertTrue(status.step in 1..6)
    }
}
