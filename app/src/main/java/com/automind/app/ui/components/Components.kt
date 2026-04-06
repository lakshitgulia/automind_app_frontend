package com.automind.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.ui.theme.*

@Composable
fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        }
    }
}

@Composable
fun LargeMetricCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        icon, contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusChip(status: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProgressBarItem(label: String, progress: Float, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = DarkSurfaceVariant
        )
    }
}

@Composable
fun DiagnosticRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    status: String = "NORMAL",
    statusColor: Color = StatusGreen
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentCyan.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
            }
            StatusChip(status = status, color = statusColor)
        }
    }
}

@Composable
fun AlertCard(
    icon: ImageVector,
    title: String,
    description: String,
    priority: String,
    priorityColor: Color,
    accentColor: Color = StatusRed,
    onLocateService: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Colored left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                    StatusChip(status = priority, color = priorityColor)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                if (onLocateService != null || onDismiss != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        onLocateService?.let {
                            Text(
                                "LOCATE SERVICE",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        onDismiss?.let {
                            Text(
                                "IGNORE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (enabled) listOf(AccentCyan, AccentCyan.copy(alpha = 0.7f))
                        else listOf(DarkSurfaceVariant, DarkSurfaceVariant)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = if (enabled) DarkBackground else TextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun OutlinedCyanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun UserAvatar(
    name: String,
    size: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar()?.toString() }
        .joinToString("")
        .ifEmpty { "U" }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(AccentCyan, AccentCyan.copy(alpha = 0.5f))
                )
            )
            .border(2.dp, AccentCyan.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = DarkBackground,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.35f).sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        ),
        color = TextSecondary,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun PulsingDot(color: Color = AccentCyan, size: Dp = 8.dp) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}
