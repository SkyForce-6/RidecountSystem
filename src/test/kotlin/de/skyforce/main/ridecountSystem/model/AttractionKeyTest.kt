package de.skyforce.main.ridecountSystem.model

import kotlin.test.Test
import kotlin.test.assertEquals

class AttractionKeyTest {

    @Test
    fun `normalizes display names to stable yaml keys`() {
        assertEquals("blue_fire", AttractionKey.fromDisplayName(" Blue Fire "))
        assertEquals("wildwasserbahn_1", AttractionKey.fromDisplayName("Wildwasserbahn #1"))
        assertEquals("tcc-coaster", AttractionKey.fromDisplayName("TCC-Coaster"))
    }
}
