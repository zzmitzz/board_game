package com.alantech.boardgame.config

import com.alantech.boardgame.ui.model.GamePlayer





object GameSettingConfigCurrentSession {
    private var players: Set<GamePlayer> = emptySet<GamePlayer>()
    private var isTimerOn = false
    private var isNSFWOn = false
    private var isRecordMomentOn = false
    private var totalRounds = 5
    private var penalty = false
    var penaltyInput = "Takes 2 shot"

    fun setPlayers(players: Set<GamePlayer>) {
        this.players = players.also {
            players.sortedBy { it.name }
        }
    }
    fun getPlayers(): List<GamePlayer> = players.toList()
    fun getIsTimerOn(): Boolean = isTimerOn
    fun getIsNSFWOn(): Boolean = isNSFWOn
    fun getIsRecordMomentOn(): Boolean = isRecordMomentOn
    fun getPenalty(): Boolean = penalty
    fun getTotalRounds(): Int = totalRounds
    fun setupGameConfig(
        isTimerOn: Boolean = false,
        isNSFWOn: Boolean = false,
        isRecordMomentOn: Boolean = false,
        penalty: Boolean = false,
        totalRounds: Int = 5
    ){
        this.isTimerOn = isTimerOn
        this.isNSFWOn = isNSFWOn
        this.isRecordMomentOn = isRecordMomentOn
        this.penalty = penalty
        this.totalRounds = totalRounds
    }


    override fun toString(): String {
        return "GameSettingConfigCurrentSession(players=$players, isTimerOn=$isTimerOn, isNSFWOn=$isNSFWOn, isRecordMomentOn=$isRecordMomentOn, penalty=$penalty, penaltyInput=$penaltyInput, totalRounds=$totalRounds)"
    }


}