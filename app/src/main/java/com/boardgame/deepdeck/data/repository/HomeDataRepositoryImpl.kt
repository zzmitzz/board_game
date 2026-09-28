package com.boardgame.deepdeck.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.boardgame.deepdeck.data.model.DailyCardResponse
import com.boardgame.deepdeck.data.model.HomeSectionDto
import com.boardgame.deepdeck.data.model.PacksPreview
import com.boardgame.deepdeck.data.model.SectionEntity
import com.boardgame.deepdeck.data.model.VibeCategory
import com.boardgame.deepdeck.data.remote.HomeDataEndpoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

class HomeDataRepositoryImpl(
    private val apiHomeData: HomeDataEndpoint,
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : HomeDataRepository {

    override suspend fun getHomeFeed(lang: String): Result<HomeFeed> = withContext(Dispatchers.IO) {
        safe {
            val feed = try {
                val response = apiHomeData.getHome(lang = lang)
                HomeFeed(
                    dailyCard = response.dailyCard,
                    vibes = response.vibes.sortedBy { it.order ?: Int.MAX_VALUE },
                    sections = response.sections.sortedBy { it.displayOrder ?: Int.MAX_VALUE },
                )
            } catch (e: HttpException) {
                // Older backend without /home (404) or a broken /home: assemble from legacy calls.
                Log.i(TAG, "/home unavailable (${e.code()}), using legacy endpoints")
                loadLegacyFeed(lang)
            } catch (e: SerializationException) {
                Log.w(TAG, "/home parse failed, using legacy endpoints", e)
                loadLegacyFeed(lang)
            }
            saveCache(feed)
            feed
        }
    }

    /** Pre-/home backends: vibes + sections + N× sections/packs (+ daily-card if available). */
    private suspend fun loadLegacyFeed(lang: String): HomeFeed = coroutineScope {
        val vibes = async { safe { apiHomeData.getAllVibes(lang) } }
        val daily = async { safe { apiHomeData.getDailyCard(lang) }.getOrNull() }
        val sectionsResult = safe { apiHomeData.getSections() }
        val sections = sectionsResult.getOrNull().orEmpty()
            .filter { it.isActive != false && it.id != null }
            .sortedBy { it.displayOrder ?: Int.MAX_VALUE }
            .map { section ->
                async {
                    val packs = safe { apiHomeData.getSectionPacks(section.id!!) }.getOrNull().orEmpty()
                    HomeSectionDto(
                        id = section.id,
                        name = section.name,
                        description = section.description,
                        uiType = section.uiType,
                        displayOrder = section.displayOrder,
                        packs = packs
                    )
                }
            }.awaitAll()
        val vibesResult = vibes.await()
        if (sectionsResult.isFailure && vibesResult.isFailure) {
            throw sectionsResult.exceptionOrNull() ?: IOException("Home unavailable")
        }
        HomeFeed(
            dailyCard = daily.await(),
            vibes = vibesResult.getOrNull().orEmpty().sortedBy { it.order ?: Int.MAX_VALUE },
            sections = sections.filter { it.packs.isNotEmpty() }
        )
    }

    override suspend fun getCachedHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        try {
            dataStore.data.first()[KEY_HOME_CACHE]?.let {
                json.decodeFromString(HomeFeed.serializer(), it).copy(fromCache = true)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "home cache unreadable", e)
            null
        }
    }

    private suspend fun saveCache(feed: HomeFeed) {
        try {
            val encoded = json.encodeToString(HomeFeed.serializer(), feed.copy(fromCache = false))
            dataStore.edit { it[KEY_HOME_CACHE] = encoded }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "home cache write failed", e)
        }
    }

    override suspend fun getDailyCard(lang: String): Result<DailyCardResponse?> = withContext(Dispatchers.IO) {
        safe {
            try {
                apiHomeData.getDailyCard(lang)
            } catch (e: HttpException) {
                if (e.code() == 404) null else throw e
            }
        }
    }

    override suspend fun getVibes(lang: String): Result<List<VibeCategory>> = withContext(Dispatchers.IO) {
        safe { apiHomeData.getAllVibes(lang).sortedBy { it.order ?: Int.MAX_VALUE } }
    }

    override suspend fun getCardsWithVibe(categoryId: String): Result<List<PacksPreview>> =
        withContext(Dispatchers.IO) { safe { apiHomeData.getCardsWithVibe(categoryId) } }

    override suspend fun getSections(): Result<List<SectionEntity>> =
        withContext(Dispatchers.IO) { safe { apiHomeData.getSections() } }

    override suspend fun getSectionDetail(sectionID: String): Result<SectionEntity> =
        withContext(Dispatchers.IO) { safe { apiHomeData.getSectionDetail(sectionID) } }

    override suspend fun getSectionPacks(sectionId: String): Result<List<PacksPreview>> =
        withContext(Dispatchers.IO) { safe { apiHomeData.getSectionPacks(sectionId) } }

    private inline fun <T> safe(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private companion object {
        const val TAG = "HomeDataRepository"
        val KEY_HOME_CACHE = stringPreferencesKey("pref_home_feed_cache")
    }
}
