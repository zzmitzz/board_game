package com.alantech.boardgame.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteBoardGame

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CustomPackLocally
