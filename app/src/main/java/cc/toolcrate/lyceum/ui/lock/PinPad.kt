package cc.toolcrate.lyceum.ui.lock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cc.toolcrate.lyceum.domain.PinRules
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

/** Dots for the digits typed so far, with a quiet number pad beneath. */
@Composable
fun PinPad(
    pin: String,
    onPin: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean = true,
    extraKey: (@Composable () -> Unit)? = null,
) {
    val paper = LyceumTheme.paper
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.semantics { contentDescription = "${pin.length} digits entered" },
        ) {
            repeat(maxOf(PinRules.MIN_LENGTH, pin.length)) { i ->
                Box(
                    Modifier.size(12.dp).let {
                        if (i < pin.length) it.background(paper.ink, CircleShape) else it.border(1.dp, paper.muted, CircleShape)
                    },
                )
            }
        }
        Gap(32)
        val rows = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("x", "0", "ok"))
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { key ->
                    Key(
                        label = when (key) { "x" -> "⌫"; "ok" -> "OK"; else -> key },
                        description = when (key) { "x" -> "Delete"; "ok" -> "Enter"; else -> key },
                        enabled = enabled && when (key) {
                            "x" -> pin.isNotEmpty()
                            "ok" -> pin.length >= PinRules.MIN_LENGTH
                            else -> pin.length < PinRules.MAX_LENGTH
                        },
                        onClick = {
                            when (key) {
                                "x" -> onPin(pin.dropLast(1))
                                "ok" -> onSubmit()
                                else -> onPin(pin + key)
                            }
                        },
                    )
                }
            }
            Gap(12)
        }
        extraKey?.invoke()
    }
}

@Composable
private fun Key(label: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    val paper = LyceumTheme.paper
    Box(
        Modifier
            .size(68.dp)
            .border(0.5.dp, paper.hairline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = if (label.length == 1) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.labelLarge,
            color = if (enabled) paper.ink else paper.hairline,
        )
    }
}
