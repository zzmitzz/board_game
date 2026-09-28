package com.boardgame.deepdeck.onboarding.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.ui.components.GlassSurface
import com.boardgame.deepdeck.ui.components.GlowBackground
import com.boardgame.deepdeck.ui.components.PrimaryButton
import com.boardgame.deepdeck.ui.components.ToggleRow
import com.boardgame.deepdeck.ui.components.VibeTile
import com.boardgame.deepdeck.ui.theme.BoardGameTheme
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.VibeGradients
import com.boardgame.deepdeck.utils.rememberNotificationPermissionRequest
import com.boardgame.deepdeck.work.EveningReminder
import com.boardgame.deepdeck.work.WorkerEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

/**
 * Last onboarding page (plan §3.3): "Who do you usually play with?" pre-selects a vibe (its
 * tile is shown first on Home) and offers the evening-reminder opt-in (asks POST_NOTIFICATIONS
 * on API 33+).
 */
class OnboardingFinalFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BoardGameTheme {
                    FinalPage(onDone = ::finish)
                }
            }
        }

    private fun finish(vibe: String?, remind: Boolean) {
        val app = requireContext().applicationContext
        val settings = EntryPointAccessors.fromApplication(app, WorkerEntryPoint::class.java).appSettingsRepository()
        viewLifecycleOwner.lifecycleScope.launch {
            settings.setPreferredVibe(vibe)
            settings.setNotifications(remind)
            if (remind) EveningReminder.schedule(app, replace = true)
            (activity as? OnboardingFragment.OnboardingCompleteListener)?.onOnboardingComplete()
        }
    }
}

private data class VibeChoice(val key: String, val label: Int, val colors: Pair<Color, Color>)

private val choices = listOf(
    VibeChoice("friends", R.string.vibe_friends, VibeGradients.Friends),
    VibeChoice("party", R.string.vibe_party, VibeGradients.Party),
    VibeChoice("drinking", R.string.vibe_drinking, VibeGradients.Drinking),
    VibeChoice("love", R.string.vibe_love, VibeGradients.Love),
)

@Composable
private fun FinalPage(onDone: (vibe: String?, remind: Boolean) -> Unit) {
    val colors = DeepTalkTheme.colors
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    var remind by rememberSaveable { mutableStateOf(true) }
    val requestPermission = rememberNotificationPermissionRequest { granted -> onDone(selected, granted) }

    GlowBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 72.dp, bottom = 32.dp)
        ) {
            Text(stringResource(R.string.onboarding_final_title), style = DeepTalkTheme.type.hero, color = colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.onboarding_final_subtitle), style = DeepTalkTheme.type.body, color = colors.textSecondary)
            Spacer(Modifier.height(24.dp))
            choices.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { choice ->
                        val isSelected = selected == choice.key
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1.25f)
                                .semantics { this.selected = isSelected }
                        ) {
                            VibeTile(
                                name = stringResource(choice.label),
                                iconKey = choice.key,
                                colorStart = choice.colors.first,
                                colorEnd = choice.colors.second,
                                onClick = { selected = if (isSelected) null else choice.key },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, Color.White, DeepTalkShapes.lg)
                                        else Modifier
                                    )
                            )
                            if (isSelected) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(12.dp))
            GlassSurface(Modifier.fillMaxWidth()) {
                ToggleRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = stringResource(R.string.onboarding_reminder_title),
                    description = stringResource(R.string.onboarding_reminder_desc),
                    checked = remind,
                    onCheckedChange = { remind = it },
                    accent = colors.gold
                )
            }
            Spacer(Modifier.weight(1f, fill = false))
            Spacer(Modifier.height(32.dp))
            PrimaryButton(
                text = stringResource(R.string.get_started),
                onClick = { if (remind) requestPermission() else onDone(selected, false) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
