package mega.privacy.android.app.presentation.meeting.chat.view.navigation

import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import mega.privacy.android.navigation.MegaNavigatorEntryPoint

internal fun openContactInfoActivity(
    context: Context,
    email: String,
) {
    EntryPointAccessors.fromApplication(context, MegaNavigatorEntryPoint::class.java)
        .megaNavigator
        .openContactInfoActivity(context, email)
}
