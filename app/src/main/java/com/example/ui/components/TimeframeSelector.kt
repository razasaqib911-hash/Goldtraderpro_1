package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Timeframe
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepPurpleBorder
import com.example.ui.theme.DeepPurpleDark
import com.example.ui.theme.DeepPurpleMid
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateGrayText

@Composable
fun TimeframeSelector(
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepPurpleDark)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Timeframe.entries.forEach { tf ->
            val isSelected = tf == selectedTimeframe
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) EmeraldGreen.copy(alpha = 0.2f) else DeepPurpleMid,
                        RoundedCornerShape(6.dp)
                    )
                    .border(
                        1.dp,
                        if (isSelected) EmeraldGreen else DeepPurpleBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectTimeframe(tf) }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .testTag("timeframe_${tf.label}")
            ) {
                Text(
                    text = tf.label,
                    color = if (isSelected) EmeraldGreen else SlateGrayText,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
