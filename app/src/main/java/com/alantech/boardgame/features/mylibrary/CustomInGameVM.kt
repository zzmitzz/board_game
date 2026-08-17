package com.alantech.boardgame.features.mylibrary

import android.content.Context
import com.alantech.boardgame.data.local.GameResultRepository
import com.alantech.boardgame.data.repository.BoardGameRepository
import com.alantech.boardgame.di.CustomPackLocally
import com.alantech.boardgame.features.ingame.InGameVM
import com.alantech.boardgame.utils.DataStoreUtils
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
