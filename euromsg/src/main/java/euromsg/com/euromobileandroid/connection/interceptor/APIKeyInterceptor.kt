package euromsg.com.euromobileandroid.connection.interceptor


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class APIKeyInterceptor @Inject constructor() : Interceptor {
    @Throws(IOException::class)

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val headers = request.headers().newBuilder().add("Authorization", "Bearer ").build()
        request = request.newBuilder().headers(headers).build()
        return chain.proceed(request)
    }
}