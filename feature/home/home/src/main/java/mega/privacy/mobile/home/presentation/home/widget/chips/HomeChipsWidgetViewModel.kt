package mega.privacy.mobile.home.presentation.home.widget.chips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import mega.privacy.android.core.coroutine.asUiStateFlow
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.domain.usecase.featureflag.GetFeatureFlagValueUseCase
import mega.privacy.android.domain.usecase.preference.MonitorNavigationItemsPreferenceUseCase
import mega.privacy.mobile.home.presentation.home.widget.chips.model.HomeChipsUiState
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Home shortcut chips widget
 */
@HiltViewModel
class HomeChipsWidgetViewModel @Inject constructor(
    private val getFeatureFlagValueUseCase: GetFeatureFlagValueUseCase,
    private val monitorNavigationItemsPreferenceUseCase: MonitorNavigationItemsPreferenceUseCase,
) : ViewModel() {

    val uiState: StateFlow<HomeChipsUiState> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
            monitorNavigationItemsPreferenceUseCase(),
            flow { emit(getFeatureFlagValueUseCase(ApiFeatures.CustomisableBottomNavigation)) },
        ) { preference, isCustomisationEnabled ->
            HomeChipsUiState(
                hiddenSectionIds = if (isCustomisationEnabled) {
                    preference?.orderedVisibleItemIds?.toSet().orEmpty()
                } else {
                    emptySet()
                },
            )
        }.catch { Timber.e(it, "Failed to monitor navigation items preference") }
            .asUiStateFlow(
                viewModelScope,
                HomeChipsUiState(hiddenSectionIds = emptySet()),
            )
    }
}
