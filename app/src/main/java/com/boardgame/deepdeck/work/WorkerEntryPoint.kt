package com.boardgame.deepdeck.work

import com.boardgame.deepdeck.data.prefs.AppSettingsRepository
import com.boardgame.deepdeck.data.remote.BoardGameEndpoint
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json

/**
 * Workers pull their dependencies through this entry point instead of `hilt-work`
 * (no custom WorkerFactory / manifest initializer changes needed).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun boardGameEndpoint(): BoardGameEndpoint
    fun json(): Json
    fun appSettingsRepository(): AppSettingsRepository
}
