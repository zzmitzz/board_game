package com.alantech.boardgame.features.ingame.screen

import com.alantech.boardgame.features.ingame.InGameVM

class ActiveGameScreenContractImpl(
    val viewModel: InGameVM
) : ActiveGameScreenContract {

    override fun onPauseClick() {
        viewModel.pauseGame()
    }


    override fun onComplete() {
        viewModel.completeCard()
    }

    override fun onForfeit() {
        viewModel.completeCard()
    }
}