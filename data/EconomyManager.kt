package com.neon.peggame.data

import kotlinx.coroutines.flow.MutableStateFlow

class EconomyManager {

    val gooBucks = MutableStateFlow(0)

    fun reward(levelId: Int, pegsLeft: Int) {
        val reward = (50 + (10 * pegsLeft))
        gooBucks.value += reward
    }
}
