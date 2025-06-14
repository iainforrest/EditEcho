package com.editecho.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardAwareDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val imeHeight = WindowInsets.ime.getBottom(density)
    var dropdownOffset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

    LaunchedEffect(imeHeight, expanded) {
        if (expanded && imeHeight > 0) {
            dropdownOffset = DpOffset(0.dp, (-imeHeight / density.density).dp)
        } else {
            dropdownOffset = DpOffset(0.dp, 0.dp)
        }
    }

    Box(modifier = modifier) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            offset = dropdownOffset,
            content = content
        )
    }
} 