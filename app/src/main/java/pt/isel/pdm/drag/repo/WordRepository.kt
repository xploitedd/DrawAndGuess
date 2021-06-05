package pt.isel.pdm.drag.repo

import pt.isel.pdm.drag.services.WordService
import java.util.*

enum class WordLanguage(val langId: String, val validLocales: Array<String>) {

    EN("en", arrayOf("en_US", "en_GB")),
    PT("pt", arrayOf("pt_PT", "pt_BR"));

    companion object {

        val FALLBACK_LANG = EN

        /**
         * Converts a Locale object to a WordLanguage
         * @param locale Locale to be converted
         * @return the WordLanguage if a locale can be converted, or the fallback WordLanguage
         */
        fun languageFromLocale(locale: Locale): WordLanguage {
            val localeStr = locale.toString()
            values().forEach {
                val isValid = it.validLocales.any { valid -> localeStr == valid }
                if (isValid)
                    return it
            }

            return FALLBACK_LANG
        }

    }

}

class WordRepository(private val service: WordService) {

    /**
     * Get a random word with the specified language
     * @param wordLanguage language of the word
     * @return the word object
     */
    suspend fun getRandomWord(wordLanguage: WordLanguage = WordLanguage.FALLBACK_LANG) =
        service.getRandomWord(wordLanguage.langId)

    /**
     * Get a word by its id
     * @param id id of the word
     * @return a word object
     */
    suspend fun getWordById(id: Int) = service.getWordById(id)

}