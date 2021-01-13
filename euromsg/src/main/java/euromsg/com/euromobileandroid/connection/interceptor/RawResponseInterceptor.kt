package euromsg.com.euromobileandroid.connection.interceptor


import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws


open class RawResponseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        return chain.proceed(req)
    }
}