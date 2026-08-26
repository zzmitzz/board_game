package com.boardgame.deepdeck.features.mylibrary

import android.content.Context
import com.boardgame.deepdeck.data.local.GameResultRepository
import com.boardgame.deepdeck.data.repository.BoardGameRepository
import com.boardgame.deepdeck.di.CustomPackLocally
import com.boardgame.deepdeck.features.ingame.InGameVM
import com.boardgame.deepdeck.utils.DataStoreUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class CustomInGameVM @Inject constructor(
    @ApplicationContext context: Context,
    @CustomPackLocally repository: BoardGameRepository,
    gameResultRepository: GameResultRepository,
    dataStoreUtils: DataStoreUtils,
) : InGameVM(context, repository, gameResultRepository, dataStoreUtils)
