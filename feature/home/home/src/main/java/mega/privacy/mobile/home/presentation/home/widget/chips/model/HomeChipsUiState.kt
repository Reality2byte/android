package mega.privacy.mobile.home.presentation.home.widget.chips.model

import androidx.compose.runtime.Stable

/**
 * UI state for the Home shortcut chips widget
 *
 * @property hiddenSectionIds ids of the sections currently shown in the bottom navigation bar;
 * chips linked to any of these sections are hidden as they would be redundant shortcuts
 */
@Stable
data class HomeChipsUiState(
    val hiddenSectionIds: Set<String>,
)
