package ru.yourok.torrserve.utils

import android.annotation.SuppressLint
import android.net.Uri
import org.jsoup.Connection
import org.jsoup.Jsoup
import ru.yourok.torrserve.atv.Utils.isBrokenTCL
import ru.yourok.torrserve.settings.Settings
import java.io.InputStream
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.security.KeyManagementException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.*

object Net {
    private const val timeout = 5000 // total request timeout duration, in ms

    fun getHostUrl(path: String): String {
        val url = Settings.getHost()
        if (path.isEmpty())
            return url

        return if (url.last() == '/')
            url + path.substring(1)
        else
            url + path
    }

    fun getAuthB64(): String {
        val auth = Settings.getServerAuth()
        if (auth.isNotEmpty())
            return "Basic " + android.util.Base64.encode(auth.toByteArray(), android.util.Base64.NO_WRAP).toString(Charset.defaultCharset())
        return ""
    }

    fun uploadAuth(url: String, title: String, poster: String, category: String, data: String, file: InputStream, save: Boolean): String {
        val req = Jsoup.connect(url)
            .data("file1", "filename", file)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .method(Connection.Method.POST)
        if (!isBrokenTCL)
            req.sslSocketFactory(TlsSocketFactory())
        if (save)
            req.data("save", "true")
        req.data("title", title)
        req.data("poster", poster)
        req.data("category", category)
        req.data("data", data)

        val auth = getAuthB64()
        if (auth.isNotEmpty())
            req.header("Authorization", auth)

        val response = req.execute()
        return response.body()
    }

    fun postAuth(url: String, req: String): String {
        val conn = Jsoup.connect(url)
            .requestBody(req)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .method(Connection.Method.POST)
            .maxBodySize(0) // The default maximum is 2MB, 0 = unlimited body
        if (!isBrokenTCL)
            conn.sslSocketFactory(TlsSocketFactory())

        val auth = getAuthB64()
        if (auth.isNotEmpty())
            conn.header("Authorization", auth)

        val response = conn.execute()

        return when (response.statusCode()) {
            200 -> {
                response.body()
            }

            302 -> {
                ""
            }

            else -> {
                throw Exception(response.statusMessage())
            }
        }
    }

    fun getAuth(url: String, duration: Int = timeout): String {
        val conn = Jsoup.connect(url)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .timeout(duration)
        if (!isBrokenTCL)
            conn.sslSocketFactory(TlsSocketFactory())

        val auth = getAuthB64()
        if (auth.isNotEmpty())
            conn.header("Authorization", auth)

        val response = conn.execute()
        return when (response.statusCode()) {
            200 -> {
                response.body()
            }

            302 -> {
                ""
            }

            else -> {
                throw Exception(response.statusMessage())
            }
        }
    }

    /* used for apk / server update check */
    fun get(url: String, duration: Int = timeout): String {
        val link = Uri.parse(url)
        if (link.scheme.equals("https", true)) {
            val trustAllHostnames = HostnameVerifier { _, _ ->
                true // Just allow them all
            }
            HttpsURLConnection.setDefaultHostnameVerifier(trustAllHostnames)
        }
        val conn = Jsoup.connect(url)
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .timeout(duration)
        if (!isBrokenTCL)
            conn.sslSocketFactory(TlsSocketFactory())

        val response = conn.execute()

        return when (response.statusCode()) {
            200 -> {
                response.body()
            }

            302 -> {
                ""
            }

            else -> {
                throw Exception(response.statusMessage())
            }
        }
    }

    fun isValidPublicIp4(ip: String?): Boolean {
        val address: Inet4Address? = try {
            InetAddress.getByName(ip) as? Inet4Address
        } catch (exception: UnknownHostException) {
            return false // assuming no logging, exception handling required
        }
        if (address != null) {
            return !(address.isSiteLocalAddress ||
                    address.isAnyLocalAddress ||
                    address.isLinkLocalAddress ||
                    address.isLoopbackAddress ||
                    address.isMulticastAddress)
        }
        return false
    }
}