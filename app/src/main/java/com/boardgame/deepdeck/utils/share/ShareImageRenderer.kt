package com.boardgame.deepdeck.utils.share

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Picture
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.boardgame.deepdeck.R
import com.boardgame.deepdeck.data.model.CardLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/** A question card to render as a 9:16 story image. */
data class ShareCardData(
    val text: String,
    val level: CardLevel,
    val packTitle: String? = null,
    /** Small caps line above the card, e.g. "Tonight's card". Defaults to the level label. */
    val overline: String? = null,
)

data class ShareAwardLine(val emoji: String, val title: String, val playerName: String, val stat: String?)

/** Session recap ("Wrapped") to render as a 9:16 story image. */
data class ShareRecapData(
    val packTitle: String,
    val winnerName: String,
    val winnerAvatar: String,
    val winnerColor: Int,
    val winnerStat: String,
    val awards: List<ShareAwardLine>,
    val cardOfTheNight: String?,
    val statsLine: String,
)

/**
 * Renders branded 1080×1920 share images (plan §7.2.8/§7.2.10) with android.graphics:
 * the scene is recorded into a [Picture] and drawn into a bitmap, so it works from any
 * screen or ViewModel without an on-screen composable. Files go to `cache/shared/` and are
 * exposed through the app FileProvider.
 */
@Singleton
class ShareImageRenderer @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val serif: Typeface by lazy {
        runCatching { ResourcesCompat.getFont(context, R.font.fraunces_semibold) }.getOrNull()
            ?: Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    private val sans: Typeface by lazy {
        runCatching { ResourcesCompat.getFont(context, R.font.plus_jakarta_sans) }.getOrNull()
            ?: Typeface.SANS_SERIF
    }
    private val sansBold: Typeface by lazy { Typeface.create(sans, Typeface.BOLD) }

    suspend fun renderCard(card: ShareCardData): Uri? = render("card") { canvas ->
        drawCardScene(canvas, card)
    }

    suspend fun renderRecap(recap: ShareRecapData): Uri? = render("recap") { canvas ->
        drawRecapScene(canvas, recap)
    }

    // -----------------------------------------------------------------------
    // Pipeline
    // -----------------------------------------------------------------------

    private suspend fun render(prefix: String, draw: (Canvas) -> Unit): Uri? = withContext(Dispatchers.IO) {
        try {
            // Warm the (possibly downloadable) fonts off the main thread.
            serif; sansBold
            val picture = Picture()
            draw(picture.beginRecording(W, H))
            picture.endRecording()
            val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).drawPicture(picture)
            val dir = File(context.cacheDir, "shared").apply { mkdirs() }
            // Keep the cache small: one file per kind.
            dir.listFiles()?.filter { it.name.startsWith(prefix) }?.forEach { it.delete() }
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            Log.w(TAG, "share image failed: ${e.message}")
            null
        }
    }

    // -----------------------------------------------------------------------
    // Scenes
    // -----------------------------------------------------------------------

    private fun drawBackground(canvas: Canvas, accent: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = LinearGradient(0f, 0f, 0f, H.toFloat(), BG_TOP, BG_BOTTOM, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), paint)
        drawGlow(canvas, 140f, 120f, 900f, withAlpha(accent, 0x55))
        drawGlow(canvas, W - 60f, H - 260f, 820f, withAlpha(BRAND_STRONG, 0x40))
    }

    private fun drawGlow(canvas: Canvas, cx: Float, cy: Float, r: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.shader = RadialGradient(cx, cy, r, color, 0x00000000, Shader.TileMode.CLAMP)
        canvas.drawCircle(cx, cy, r, paint)
    }

    private fun drawWordmark(canvas: Canvas, y: Float) {
        val paint = textPaint(sansBold, 34f, BRAND).apply {
            letterSpacing = 0.35f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DEEPTALK", W / 2f, y, paint)
    }

    private fun drawFooter(canvas: Canvas, line: String) {
        val paint = textPaint(sans, 32f, TEXT_MUTED).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(line, W / 2f, H - 90f, paint)
    }

    private fun drawCardScene(canvas: Canvas, card: ShareCardData) {
        val level = levelColor(card.level)
        drawBackground(canvas, level)
        drawWordmark(canvas, 190f)

        val rect = RectF(90f, 330f, W - 90f, 1520f)
        drawCardFace(canvas, rect, level)

        // Level / overline pill
        val pillText = (card.overline ?: levelLabel(card.level)).uppercase()
        drawPill(canvas, rect.left + 72f, rect.top + 80f, pillText, level)

        // Question, auto-shrunk to fit
        val inner = RectF(rect.left + 72f, rect.top + 200f, rect.right - 72f, rect.bottom - 120f)
        drawFittedText(canvas, "“${card.text}”", inner, serif, TEXT_PRIMARY, maxSize = 84f, minSize = 44f)

        card.packTitle?.takeIf { it.isNotBlank() }?.let { title ->
            val p = textPaint(sansBold, 40f, TEXT_SECONDARY).apply { textAlign = Paint.Align.CENTER }
            canvas.drawText(ellipsize(title, p, W - 200f), W / 2f, 1640f, p)
        }
        drawFooter(canvas, context.getString(R.string.share_image_footer))
    }

    private fun drawRecapScene(canvas: Canvas, recap: ShareRecapData) {
        drawBackground(canvas, GOLD)
        drawWordmark(canvas, 150f)

        val title = textPaint(serif, 84f, TEXT_PRIMARY).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(context.getString(R.string.wrapped_title), W / 2f, 260f, title)
        val sub = textPaint(sans, 38f, TEXT_SECONDARY).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(ellipsize(recap.packTitle, sub, W - 200f), W / 2f, 325f, sub)

        // Winner avatar with gold glow
        val cx = W / 2f
        val cy = 560f
        drawGlow(canvas, cx, cy, 260f, withAlpha(GOLD, 0x66))
        val avatarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = recap.winnerColor }
        canvas.drawCircle(cx, cy, 130f, avatarPaint)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 8f; color = GOLD
        }
        canvas.drawCircle(cx, cy, 138f, ring)
        val emoji = textPaint(sansBold, 130f, 0xFF1A0633.toInt()).apply { textAlign = Paint.Align.CENTER }
        val fm = emoji.fontMetrics
        canvas.drawText(recap.winnerAvatar, cx, cy - (fm.ascent + fm.descent) / 2f, emoji)

        val crown = textPaint(sansBold, 34f, GOLD).apply {
            textAlign = Paint.Align.CENTER; letterSpacing = 0.25f
        }
        canvas.drawText("🏆 " + context.getString(R.string.award_winner).uppercase(), cx, 780f, crown)
        val name = textPaint(serif, 88f, TEXT_PRIMARY).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(ellipsize(recap.winnerName, name, W - 160f), cx, 880f, name)
        val stat = textPaint(sans, 36f, TEXT_SECONDARY).apply { textAlign = Paint.Align.CENTER }
        canvas.drawText(recap.winnerStat, cx, 940f, stat)

        // Awards
        var y = 1010f
        recap.awards.take(4).forEach { award ->
            val row = RectF(90f, y, W - 90f, y + 118f)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(SURFACE, 0xC8) }
            canvas.drawRoundRect(row, 40f, 40f, fill)
            val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE; strokeWidth = 2f; color = 0x1FFFFFFF
            }
            canvas.drawRoundRect(row, 40f, 40f, stroke)
            val e = textPaint(sansBold, 58f, TEXT_PRIMARY)
            canvas.drawText(award.emoji, row.left + 36f, row.centerY() + 20f, e)
            val t = textPaint(sansBold, 30f, GOLD).apply { letterSpacing = 0.12f }
            canvas.drawText(award.title.uppercase(), row.left + 130f, row.top + 50f, t)
            val n = textPaint(sansBold, 40f, TEXT_PRIMARY)
            val nameText = ellipsize(award.playerName, n, 460f)
            canvas.drawText(nameText, row.left + 130f, row.top + 96f, n)
            award.stat?.let {
                val s = textPaint(sans, 30f, TEXT_SECONDARY).apply { textAlign = Paint.Align.RIGHT }
                canvas.drawText(ellipsize(it, s, 300f), row.right - 36f, row.centerY() + 12f, s)
            }
            y += 136f
        }

        // Card of the night
        recap.cardOfTheNight?.takeIf { it.isNotBlank() }?.let { text ->
            val top = maxOf(y + 20f, 1420f)
            val rect = RectF(90f, top, W - 90f, H - 170f)
            if (rect.height() > 220f) {
                drawCardFace(canvas, rect, LEVEL_DEEP)
                drawPill(canvas, rect.left + 48f, rect.top + 56f, context.getString(R.string.card_of_the_night).uppercase(), ROSE)
                val inner = RectF(rect.left + 48f, rect.top + 130f, rect.right - 48f, rect.bottom - 40f)
                drawFittedText(canvas, "“$text”", inner, serif, TEXT_PRIMARY, maxSize = 50f, minSize = 30f)
            }
        }
        drawFooter(canvas, recap.statsLine)
    }

    // -----------------------------------------------------------------------
    // Primitives
    // -----------------------------------------------------------------------

    private fun drawCardFace(canvas: Canvas, rect: RectF, glow: Int) {
        val face = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, rect.top, 0f, rect.bottom, CARD_TOP, CARD_BOTTOM, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, 64f, 64f, face)
        canvas.save()
        val clip = android.graphics.Path().apply { addRoundRect(rect, 64f, 64f, android.graphics.Path.Direction.CW) }
        canvas.clipPath(clip)
        drawGlow(canvas, rect.left, rect.top, rect.width() * 0.95f, withAlpha(glow, 0x40))
        canvas.restore()
        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE; strokeWidth = 3f; color = 0x1AFFFFFF
        }
        canvas.drawRoundRect(rect, 64f, 64f, border)
    }

    private fun drawPill(canvas: Canvas, left: Float, centerY: Float, text: String, color: Int) {
        val p = textPaint(sansBold, 28f, color).apply { letterSpacing = 0.18f }
        val w = p.measureText(text) + 56f
        val rect = RectF(left, centerY - 30f, left + w, centerY + 30f)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = withAlpha(color, 0x2E) }
        canvas.drawRoundRect(rect, 30f, 30f, fill)
        canvas.drawText(text, left + 28f, centerY + 10f, p)
    }

    /** Draws [text] vertically centered in [box], shrinking the size until it fits. */
    private fun drawFittedText(
        canvas: Canvas,
        text: String,
        box: RectF,
        typeface: Typeface,
        color: Int,
        maxSize: Float,
        minSize: Float,
    ) {
        var size = maxSize
        var layout: StaticLayout
        while (true) {
            val paint = textPaint(typeface, size, color)
            layout = staticLayout(text, paint, box.width().toInt())
            if (layout.height <= box.height() || size <= minSize) break
            size -= 4f
        }
        if (layout.height > box.height()) {
            val paint = textPaint(typeface, size, color)
            val maxLines = (box.height() / (layout.height.toFloat() / layout.lineCount)).toInt().coerceAtLeast(1)
            layout = staticLayout(text, paint, box.width().toInt(), maxLines)
        }
        canvas.save()
        canvas.translate(box.left, box.top + (box.height() - layout.height).coerceAtLeast(0f) / 2f)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun staticLayout(text: String, paint: TextPaint, width: Int, maxLines: Int = Int.MAX_VALUE): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.18f)
            .setIncludePad(false)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

    private fun textPaint(typeface: Typeface, size: Float, color: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = size
        this.color = color
    }

    private fun ellipsize(text: String, paint: TextPaint, width: Float): String =
        TextUtils.ellipsize(text, paint, width, TextUtils.TruncateAt.END).toString()

    private fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha shl 24)

    private fun levelColor(level: CardLevel): Int = when (level) {
        CardLevel.ICEBREAKER -> SKY
        CardLevel.DEEP -> LEVEL_DEEP
        CardLevel.INTIMATE -> ROSE
        CardLevel.UNKNOWN -> BRAND
    }

    private fun levelLabel(level: CardLevel): String = context.getString(
        when (level) {
            CardLevel.ICEBREAKER -> R.string.level_icebreaker
            CardLevel.DEEP -> R.string.level_deep
            CardLevel.INTIMATE -> R.string.level_intimate
            CardLevel.UNKNOWN -> R.string.level_question
        }
    )

    private companion object {
        const val TAG = "ShareImageRenderer"
        const val W = 1080
        const val H = 1920
        val BG_TOP = 0xFF1C0D33.toInt()
        val BG_BOTTOM = 0xFF0B0612.toInt()
        val CARD_TOP = 0xFF2A1648.toInt()
        val CARD_BOTTOM = 0xFF140A22.toInt()
        val SURFACE = 0xFF1D1430.toInt()
        val TEXT_PRIMARY = 0xFFF6F0FF.toInt()
        val TEXT_SECONDARY = 0xFFB8A7D3.toInt()
        val TEXT_MUTED = 0xFF7C6B96.toInt()
        val BRAND = 0xFFB57BFF.toInt()
        val BRAND_STRONG = 0xFF8B3DFF.toInt()
        val GOLD = 0xFFFFC76B.toInt()
        val ROSE = 0xFFFF5C8A.toInt()
        val SKY = 0xFF6FC3FF.toInt()
        val LEVEL_DEEP = 0xFF9B6BFF.toInt()
    }
}
