package juwens

import java.net.URL
import kotlin.collections.HashSet
import org.jsoup.Jsoup

fun main(args: Array<String>) {
    runCrawler()
}


fun runCrawler() {
    val baseUrl = "http://example.com/"
    val urlsCrawled = hashMapOf<URL, HashSet<URL>>()
    val urlsToCrawl = hashSetOf<URL>()
    urlsToCrawl.add(URL(baseUrl))

    while (urlsToCrawl.size > 0) {
        val urlToCrawl = urlsToCrawl.first()
        urlsToCrawl.remove(urlToCrawl)

        println("to crawl: ${urlsToCrawl.size}|crawled: ${urlsCrawled.size}")
        println("crawling: $urlToCrawl")
        Thread.sleep(1000)

        val allUrls = getLinksFromBody(urlToCrawl)
        val normalizedUrls = allUrls
            .filter{ isDerivedFromBaseUrl(baseUrl, it)}
            .map { normalizeUrl(URL(baseUrl), it) }
            .distinct()
            .filter { isAllowedToCrawl(it) }

        urlsCrawled[urlToCrawl] = normalizedUrls.toHashSet()

        for (normalizedUrl in normalizedUrls) {
            if (!urlsCrawled.containsKey(normalizedUrl)) {
                urlsToCrawl.add(normalizedUrl)
            }
        }
    }

    println(urlsCrawled)
}

fun isDerivedFromBaseUrl(baseUrl: String, it: String): Boolean {
    return it.startsWith(baseUrl)
            || it.startsWith("/")
            || it.startsWith("./")
            || it.startsWith("../")
}

fun isAllowedToCrawl(url: URL): Boolean {
    var res =
        (url.path == null || !url.path.contains(Regex("/(login|signup)", RegexOption.IGNORE_CASE)))
        && (url.query == null || !url.query.contains(Regex("(&|[?])(returnUrl|ssrc)", RegexOption.IGNORE_CASE)))
    //println("$url is nice $isNice")
    return res
}

fun normalizeUrl(baseUrl: URL, url: String): URL {
    val urlWithHost = when {
        url.startsWith("/") -> URL(baseUrl.protocol, baseUrl.host, baseUrl.port, url)
        url.startsWith(baseUrl.toString()) -> URL(url)
        else -> throw java.lang.Exception()
    }

    // remove hash "#"
    return URL(urlWithHost.protocol, urlWithHost.host, urlWithHost.port, urlWithHost.file)
}

fun getLinksFromBody(url: URL): List<String> {
    val doc = Jsoup.connect(url.toString()).get()
    val links = doc.select("a")

    return links
        .map { x -> x.attr("href") }
        .filter { it != null }
        //.onEach<String?, List<String>> { println(it) }
        .filter<String> { !isUrlMalformed(it) }
}

fun isUrlMalformed(url: String?): Boolean {
    return when (url) {
        null -> true
        else -> url.startsWith("#")
    }
}


