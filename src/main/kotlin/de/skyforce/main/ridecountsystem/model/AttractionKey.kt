package de.skyforce.main.ridecountsystem.model

object AttractionKey {

    private val unsafeCharacters = Regex("[^a-z0-9_-]")
    private val repeatedUnderscores = Regex("_+")

    fun fromDisplayName(attraction: String): String {
        return attraction
            .trim()
            .lowercase()
            .replace(unsafeCharacters, "_")
            .replace(repeatedUnderscores, "_")
            .trim('_')
    }
}
