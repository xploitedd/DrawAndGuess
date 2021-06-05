package pt.isel.pdm.drag.util

import com.google.common.util.concurrent.RateLimiter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

private const val DEFAULT_MAX_REQUESTS = 4

object CustomHttpClient {

    /**
     * Creates a custom OkHttp client with the specified settings
     * @param maxRequests max request that this client can do per second
     * @return the http client
     */
    fun createClient(maxRequests: Int = DEFAULT_MAX_REQUESTS): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(RateLimitInterceptor(maxRequests))
            .build()

}

class RateLimitInterceptor(maxRequests: Int) : Interceptor {

    // RateLimiter is a Guava beta feature
    private val limiter = RateLimiter.create(maxRequests.toDouble())

    override fun intercept(chain: Interceptor.Chain?): Response {
        limiter.acquire(1)
        return chain?.proceed(chain.request()) ?: throw Exception("An error occurred while rate limiting!")
    }

}