package com.alantech.boardgame.config

import com.alantech.boardgame.ui.model.GamePlayer





object GameSettingConfigCurrentSession {
    private var players: Set<GamePlayer> = emptySet<GamePlayer>()
    private var isTimerOn = false
    private var isNSFWOn = false
    private var isRecordMomentOn = false
    private var totalRounds = 5

    fun setPlayers(players: Set<GamePlayer>) {
        this.players = players.also {
            players.sortedBy { it.name }
        }
    }
    fun getPlayers(): List<GamePlayer> = players.toList()
    fun setupGameConfig(
        isTimerOn: Boolean = false,
        isNSFWOn: Boolean = false,
        isRecordMomentOn: Boolean = false,
        totalRounds: Int = 5
    ){
        this.isTimerOn = isTimerOn
        this.isNSFWOn = isNSFWOn
        this.isRecordMomentOn = isRecordMomentOn
        this.totalRounds = totalRounds
    }


}