package de.pse.oys.data.properties

import kotlinx.coroutines.flow.Flow

interface Properties {
    val darkmode: Flow<Darkmode>

    suspend fun setDarkmode(darkmode: Darkmode)
}
