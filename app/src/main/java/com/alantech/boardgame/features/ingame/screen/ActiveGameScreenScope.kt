package com.alantech.boardgame.features.ingame.screen

import com.alantech.boardgame.features.ingame.InGameVM

class ActiveGameScreenContractImpl(
    val viewModel: InGameVM
) : ActiveGameScreenContract {

    override fun onExitGameClick() {
        viewModel.onEndGame()
    }


    override fun onComplete() {
        viewModel.onUserDoneCard(true)
    }

    override fun onForfeit() {
        viewModel.onUserDoneCard(false)
    }

}