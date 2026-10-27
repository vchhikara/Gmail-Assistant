package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import com.example.ui.icons.SlimCheck
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.KitMotion
import com.example.ui.theme.KitShapes
import com.example.ui.theme.kit

/** Clickable with the kit press shrink (150ms) and no ripple. */
fun Modifier.kitClickable(
    scaleTo: Float = 0.95f,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed && enabled) scaleTo else 1f, KitMotion.press(), label = "press")
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, enabled = enabled, onClick = onClick)
}

/** Gray-fill button. `solid` uses the green accent for the one primary action. */
@Composable
fun KitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    solid: Boolean = false,
    danger: Boolean = false,
    enabled: Boolean = true,
    spinIcon: Boolean = false,
    height: Dp = 48.dp,
) {
    val spin = rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing)),
        label = "spinAngle",
    )
    val bg by animateColorAsState(if (solid) kit.accent else kit.fill, KitMotion.fill(), label = "btnBg")
    val fg = when {
        solid -> kit.onAccent
        danger -> kit.danger
        else -> kit.text
    }
    val alpha by animateFloatAsState(if (enabled) 1f else 0.45f, KitMotion.fill(), label = "btnAlpha")
    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .kitClickable(enabled = enabled, onClick = onClick)
            .clip(KitShapes.control)
            .background(bg)
            .defaultMinSize(minHeight = height)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = if (spinIcon) spin.value else 0f },
            )
            Box(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun KitIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    size: Dp = 40.dp,
) {
    Box(
        modifier = modifier
            .kitClickable(onClick = onClick)
            .size(size)
            .clip(KitShapes.control)
            .background(kit.fill),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (danger) kit.danger else kit.text,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** Neutral pill used for categories and status. */
@Composable
fun KitPill(text: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Text(
        text = text,
        color = if (accent) kit.onAccent else kit.text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(if (accent) kit.accent else kit.fillStrong)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** Small count chip shown next to a segmented option. */
@Composable
fun CountBadge(count: Int, selected: Boolean) {
    val bg by animateColorAsState(
        if (selected) Color.Black.copy(alpha = 0.12f) else kit.fillStrong,
        KitMotion.fill(),
        label = "badge",
    )
    Text(
        text = count.toString(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (selected) kit.onAccent else kit.text,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 16.dp, minHeight = 16.dp)
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 4.dp),
    )
}

/**
 * Segmented tray with a green thumb that slides to the selected option (300ms).
 * `content` draws each option; `tags` optionally names each cell for tests.
 */
@Composable
fun KitSegmented(
    count: Int,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trayShape: Shape = KitShapes.control,
    thumbShape: Shape = KitShapes.controlThumb,
    cellHeight: Dp = 40.dp,
    padding: Dp = 4.dp,
    tags: List<String>? = null,
    content: @Composable (index: Int, selected: Boolean) -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .clip(trayShape)
            .background(kit.fill)
            .padding(padding),
    ) {
        val cell = maxWidth / count
        val thumbX by animateDpAsState(cell * selected, KitMotion.slide(), label = "thumb")
        Box(
            Modifier
                .offset(x = thumbX)
                .width(cell)
                .height(cellHeight)
                .clip(thumbShape)
                .background(kit.accent),
        )
        Row {
            for (i in 0 until count) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .clip(thumbShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onSelect(i) }
                        .then(if (tags != null) Modifier.testTag(tags[i]) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    content(i, i == selected)
                }
            }
        }
    }
}

/** Text option for `KitSegmented`, with an optional count badge. */
@Composable
fun SegmentLabel(text: String, selected: Boolean, badge: Int? = null) {
    val color by animateColorAsState(
        if (selected) kit.onAccent else kit.text.copy(alpha = 0.7f),
        KitMotion.fill(),
        label = "segText",
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        if (badge != null) CountBadge(badge, selected)
    }
}

/** Checkbox: gray fill idle, green checked, spring check mark. */
@Composable
fun KitCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val bg by animateColorAsState(if (checked) kit.accent else kit.fillStrong, KitMotion.fill(), label = "cbBg")
    val mark by animateFloatAsState(if (checked) 1f else 0f, KitMotion.spring(), label = "cbMark")
    Box(
        modifier = modifier
            .kitClickable(scaleTo = 0.9f) { onCheckedChange(!checked) }
            .size(24.dp)
            .clip(KitShapes.check)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            SlimCheck,
            contentDescription = null,
            tint = kit.onAccent,
            modifier = Modifier
                .size(16.dp)
                .graphicsLayer { scaleX = mark; scaleY = mark },
        )
    }
}

@Composable
fun Modifier.kitCard(color: Color = kit.card): Modifier =
    this.fillMaxWidth().clip(KitShapes.card).background(color)

@Composable
fun mutedText(text: String, modifier: Modifier = Modifier, size: Int = 13) {
    Text(text, color = kit.muted, fontSize = size.sp, lineHeight = (size + 5).sp, modifier = modifier)
}
