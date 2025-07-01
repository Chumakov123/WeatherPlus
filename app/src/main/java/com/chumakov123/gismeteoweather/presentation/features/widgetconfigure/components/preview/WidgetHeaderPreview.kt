package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance

@Composable
fun WidgetHeaderPreview(
    placeName: String,
    updateTimeText: String?,
    isLoading: Boolean,
    appearance: WidgetAppearance,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(start = 8.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = buildString {
                updateTimeText?.let { append("$it, ") }
                append(placeName)
            },
            style = TextStyle(
                fontSize = 12.sp * appearance.textScale,
                color = Color.White,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier
                .weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_settings),
                contentDescription = "Настройки",
                modifier = Modifier
                    .size(16.dp, 12.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = "Переключить режим",
                modifier = Modifier
                    .size(16.dp, 12.dp),
                tint = Color.Unspecified
            )
            Spacer(Modifier.width(8.dp))
            if (!isLoading) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = "Обновить",
                    modifier = Modifier
                        .size(16.dp, 12.dp),
                    tint = Color.Unspecified
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp, 12.dp),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
