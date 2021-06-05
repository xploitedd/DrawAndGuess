package pt.isel.pdm.drag.services

import com.google.gson.annotations.SerializedName
import pt.isel.pdm.drag.util.CustomHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val MAX_REQUESTS_PER_SECOND = 6
private const val API_VERSION = "v1"
private const val BASE_URL = "https://dragapi.xploited.xyz/$API_VERSION/"
private const val DEFAULT_LANGUAGE = "en"

interface WordService {

    companion object {

        /**
         * Gets an instance of the WordService
         * @return a WordService instance created by retrofit
         */
        fun getInstance(): WordService {
            val retrofit = Retrofit.Builder()
                .client(CustomHttpClient.createClient(MAX_REQUESTS_PER_SECOND))
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(WordService::class.java)
        }

    }

    @GET("word")
    suspend fun getRandomWord(@Query("lang") lang: String = DEFAULT_LANGUAGE): WordObject

    @GET("word/{id}")
    suspend fun getWordById(@Path("id") wordId: Int, @Query("lang") lang: String = DEFAULT_LANGUAGE): WordObject

}

data class WordObject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("word")
    val word: String
)