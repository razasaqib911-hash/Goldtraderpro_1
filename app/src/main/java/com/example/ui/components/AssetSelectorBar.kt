package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssetCategory
import com.example.data.model.AssetPair
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleElevated
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText
import java.util.Locale

@Composable
fun AssetSelectorBar(
    currentPair: AssetPair,
    currentPrice: Double,
    onSelectPair: (AssetPair) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(currentPair.category) }
    val filteredPairs = remember(selectedCategory) {
        AssetPair.ALL_PAIRS.filter { it.category == selectedCategory }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepPurpleDark)
            .padding(vertical = 4.dp)
    ) {
        // Category Tabs: FOREX (10) vs CRYPTO (10)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (selectedCategory == AssetCategory.FOREX) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleMid,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (selectedCategory == AssetCategory.FOREX) EmeraldGreen else DeepPurpleBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedCategory = AssetCategory.FOREX }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("tab_forex")
            ) {
                Text(
                    text = "FOREX (10)",
                    color = if (selectedCategory == AssetCategory.FOREX) EmeraldGreen else SlateGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(
                        if (selectedCategory == AssetCategory.CRYPTO) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleMid,
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (selectedCategory == AssetCategory.CRYPTO) EmeraldGreen else DeepPurpleBorder,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { selectedCategory = AssetCategory.CRYPTO }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("tab_crypto")
            ) {
                Text(
                    text = "CRYPTO (10)",
                    color = if (selectedCategory == AssetCategory.CRYPTO) EmeraldGreen else SlateGrayText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Active pair badge
            Text(
                text = "${currentPair.symbol} Live",
                color = AmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Horizontal Carousel of Pairs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filteredPairs.forEach { pair ->
                val isSelected = pair.symbol == currentPair.symbol
                val displayPrice = if (isSelected) currentPrice else pair.defaultPrice

                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) EmeraldGreen.copy(alpha = 0.25f) else DeepPurpleMid,
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (isSelected) EmeraldGreen else DeepPurpleBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectPair(pair) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("pair_chip_${pair.symbol.replace("/", "_")}")
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = pair.symbol,
                                color = if (isSelected) CrispWhite else SlateGrayText,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "%.${pair.decimals}f", displayPrice),
                            color = if (isSelected) EmeraldGreen else CrispWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
