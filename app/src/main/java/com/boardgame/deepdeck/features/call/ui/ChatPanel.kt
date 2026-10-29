package com.boardgame.deepdeck.features.call.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.features.call.ChatItem
import com.boardgame.deepdeck.features.call.ChatStatus
import com.boardgame.deepdeck.ui.components.GlassIconButton
import com.boardgame.deepdeck.ui.components.pressable
import com.boardgame.deepdeck.ui.theme.DeepTalkShapes
import com.boardgame.deepdeck.ui.theme.DeepTalkTheme
import com.boardgame.deepdeck.ui.theme.Spacing
import java.util.Date

/** The chat sheet over the video: messages, "typing…", and the input. */
@Composable
fun ChatPanel(
    messages: List<ChatItem>,
    peerName: String?,
    peerTyping: Boolean,
    onSend: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onRetry: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepTalkTheme.colors
    val listState = rememberLazyListState()
    var draft by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(messages.size, peerTyping) {
        val last = messages.size - 1 + if (peerTyping) 1 else 0
        if (last >= 0) listState.animateScrollToItem(last)
    }

    Column(
        modifier
            .background(colors.bgElevated, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            // Inside the background, so the sheet reaches the bottom edge behind the nav bar / keyboard.
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.call_chat),
                style = DeepTalkTheme.type.headline,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            GlassIconButton(
                icon = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.call_chat_close),
                onClick = onClose,
                size = 40.dp
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (messages.isEmpty() && !peerTyping) {
                item(key = "empty") {
                    Box(Modifier.fillMaxWidth().padding(vertical = Spacing.xxl), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.call_chat_empty), style = DeepTalkTheme.type.body, color = colors.textMuted)
                    }
                }
            }
            items(messages, key = { it.id }) { message ->
                ChatBubble(message, onRetry = { onRetry(message.id) })
            }
            if (peerTyping && peerName != null) {
                item(key = "typing") {
                    Text(
                        stringResource(R.string.call_chat_typing, peerName),
                        style = DeepTalkTheme.type.label,
                        color = colors.textMuted,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(start = Spacing.md, end = Spacing.sm, top = Spacing.xs, bottom = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val send = {
                if (draft.isNotBlank()) {
                    onSend(draft)
                    draft = ""
                }
            }
            BasicTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    onInputChange(it)
                },
                textStyle = DeepTalkTheme.type.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.brand),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { send() }),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(
                        Modifier
                            .heightIn(min = 48.dp)
                            .background(colors.surface, DeepTalkShapes.lg)
                            .border(1.dp, colors.outline, DeepTalkShapes.lg)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (draft.isEmpty()) {
                            Text(stringResource(R.string.call_chat_hint), style = DeepTalkTheme.type.body, color = colors.textMuted)
                        }
                        inner()
                    }
                }
            )
            Spacer(Modifier.size(8.dp))
            GlassIconButton(
                icon = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(R.string.call_chat_send),
                onClick = send,
                active = draft.isNotBlank(),
                tint = if (draft.isNotBlank()) colors.brand else colors.textMuted,
                size = 48.dp
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatItem, onRetry: () -> Unit) {
    val colors = DeepTalkTheme.colors
    val context = LocalContext.current
    val time = DateFormat.getTimeFormat(context).format(Date(message.sentAtMillis))
    val shape = if (message.mine) {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 6.dp)
    } else {
        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 6.dp, bottomEnd = 18.dp)
    }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.mine) Alignment.End else Alignment.Start
    ) {
        if (!message.mine) {
            Text(message.name, style = DeepTalkTheme.type.overline, color = colors.textMuted, modifier = Modifier.padding(start = 6.dp, bottom = 2.dp))
        }
        Box(
            Modifier
                .widthIn(max = 280.dp)
                .alpha(if (message.status == ChatStatus.SENDING) 0.7f else 1f)
                .then(
                    if (message.mine) Modifier.background(colors.brandGradient, shape)
                    else Modifier.background(colors.surfaceHigh, shape)
                )
                .then(if (message.status == ChatStatus.FAILED) Modifier.pressable(onClick = onRetry) else Modifier)
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                message.text,
                style = DeepTalkTheme.type.body,
                color = if (message.mine) colors.brandOn else colors.textPrimary
            )
        }
        Row(Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            when (message.status) {
                ChatStatus.FAILED -> Text(
                    stringResource(R.string.call_chat_failed),
                    style = DeepTalkTheme.type.label,
                    color = colors.rose,
                    modifier = Modifier.pressable(onClick = onRetry)
                )
                ChatStatus.SENDING -> {
                    Icon(Icons.Rounded.Schedule, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.size(4.dp))
                    Text(time, style = DeepTalkTheme.type.label, color = colors.textMuted)
                }
                ChatStatus.SENT -> Text(time, style = DeepTalkTheme.type.label, color = colors.textMuted)
            }
        }
    }
}
