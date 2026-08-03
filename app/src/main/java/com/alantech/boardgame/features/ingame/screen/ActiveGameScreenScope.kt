package com.alantech.boardgame.features.ingame.screen

import com.alantech.boardgame.features.ingame.InGameVM

class ActiveGameScreenContractImpl(
    val viewModel: InGameVM
) : ActiveGameScreenContract {

    private val TIME_DEBOUNCE_CLICK = 800L
    private var lastClickStart = 0L

    override fun onExitGameClick() {
    }


    override fun onComplete() {
        if(System.currentTimeMillis() - lastClickStart < TIME_DEBOUNCE_CLICK) return
        lastClickStart = System.currentTimeMillis()
        viewModel.onCardComplete(true)
    }

    override fun onForfeit() {
        if(System.currentTimeMillis() - lastClickStart < TIME_DEBOUNCE_CLICK) return
        lastClickStart = System.currentTimeMillis()
        viewModel.onCardComplete(false)
    }

}