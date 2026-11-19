package cc.toolcrate.lyceum.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.time.LocalDate

/** Export to and import from a JSON file the user chooses. */
@Composable
fun DataSection(
    onBeforePicker: () -> Unit,
    onExport: (Uri) -> Unit,
    onImport: (Uri) -> Unit,
) {
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) onExport(uri)
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onImport(uri)
    }
    SettingRow(
        "Export to a file",
        "Every page and prompt as plain JSON, saved wherever you choose.",
        onClick = { onBeforePicker(); export.launch("lyceum-${LocalDate.now()}.json") },
    )
    SettingRow(
        "Import from a file",
        "Adds pages from a Lyceum export. Where a day exists in both, the newer page is kept.",
        onClick = { onBeforePicker(); import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
    )
}
