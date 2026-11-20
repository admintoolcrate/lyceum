package cc.toolcrate.lyceum.stagira

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.editor.GoalChip
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

/**
 * The editor's Stagira line. With Stagira installed (and the link switched on): a way to pick a goal,
 * and the linked goal opens in Stagira. Without it, a goal already linked is still shown and can be removed.
 */
@Composable
fun StagiraGoalSlot(goal: GoalRef?, onPick: (GoalRef?) -> Unit, enabled: Boolean, beforeLeaving: () -> Unit) {
    val context = LocalContext.current
    val paper = LyceumTheme.paper
    val canPick = remember { enabled && StagiraContract.canPick(context) }
    val canView = remember { enabled && StagiraContract.canView(context) }
    val picker = rememberLauncherForActivityResult(StagiraContract.PickGoal()) { picked -> if (picked != null) onPick(picked) }

    if (!canPick) {
        GoalChip(goal, onPick)
        return
    }
    Column {
        SectionLabel("Stagira goal · optional")
        Gap(4)
        if (goal == null) {
            TextButton(onClick = { beforeLeaving(); picker.launch(Unit) }) { Text("Link a goal", color = paper.accent) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(
                    onClick = { if (canView) { beforeLeaving(); StagiraContract.view(context, goal) } },
                    label = { Text(goal.title.ifBlank { "A goal" }) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = paper.card, labelColor = paper.ink),
                )
                IconButton(onClick = { onPick(null) }) { Icon(Icons.Filled.Close, "Unlink goal", Modifier.size(18.dp)) }
                TextButton(onClick = { beforeLeaving(); picker.launch(Unit) }) { Text("Change", color = paper.muted) }
            }
        }
    }
}
