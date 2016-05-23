package com.ilyamur.scrawler

import java.io.{FileOutputStream, InputStream, OutputStream}
import java.util.concurrent.Executors

import org.apache.commons.io.IOUtils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.{ClientProtocolException, ResponseHandler}
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object SApplication extends App {

    private val MAX_CONNECTIONS = 16
    private val URL = "http://ya.ru"

    def sequenceResourceUseReleaseAsync[R, A](r: R)(f: R => Seq[Future[A]])(c: R => Unit): Seq[Future[A]] = {
        val futures = try {
            f(r)
        } catch {
            case e: Exception =>
                c(r)
                throw e
        }
        Future.sequence(futures).onComplete { _ =>
            c(r)
        }
        futures
    }

    def createManager[A](maxConnections: Int): PoolingHttpClientConnectionManager = {
        val manager = new PoolingHttpClientConnectionManager()
        manager.setMaxTotal(maxConnections)
        manager.setDefaultMaxPerRoute(maxConnections)
        manager
    }

    def createHttpClient[A](manager: HttpClientConnectionManager): CloseableHttpClient = {
        HttpClients.custom()
                .setConnectionManager(manager)
                .build()
    }

    def onHttpClientConnectionManagerAsync[A](maxConnections: Int)(f: HttpClientConnectionManager => Seq[Future[A]]): Seq[Future[A]] = {
        sequenceResourceUseReleaseAsync(createManager(maxConnections))(f)(_.shutdown())
    }

    def onHttpClientAsync[A](maxConnections: Int)(f: CloseableHttpClient => Seq[Future[A]]): Seq[Future[A]] = {
        onHttpClientConnectionManagerAsync(maxConnections) { manager =>
            sequenceResourceUseReleaseAsync(createHttpClient(manager))(f)(_.close())
        }
    }

    val respHandler = new ResponseHandler[String] {
        override def handleResponse(resp: HttpResponse): String = {
            val status = resp.getStatusLine.getStatusCode
            if ((status >= 200) && (status < 300)) {
                val entity = resp.getEntity
                if (entity != null) {
                    EntityUtils.toString(entity)
                } else {
                    ""
                }
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status)
            }
        }
    }

    def httpGet(uri: String)(implicit hc: CloseableHttpClient, ec: ExecutionContext): Future[String] = {
        Future {
            hc.execute(new HttpGet(uri), respHandler)
        }
    }

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(16))

    val respBody = onHttpClientAsync(MAX_CONNECTIONS) { implicit httpClient =>
        Seq(
            httpGet("http://ya.ru"),
            httpGet("http://google.ru")
        )
    }

    /*

    val process = get("http://ya.ru") {
        select(".block") {
            val blockId = attr("id")
            select("img[src]") {
                val imgSrc = attr("src")
                val imgId = attr("id")
                get(imgSrc) {
                    log(s"saving ${blockId}/${imgId}")
                    toFile(s"C:/temp/${blockId}/${imgId}.png")
                    // toOutputStream(s"${blockId}/${imgId}", buildOutputStream(blockId, imgId)).thenClose()
                }
                get(imgSrc, imgSrc) { case (ctx1, ctx2) =>
                    // select("img") // error!
                    ctx1.select.attr("img")
                    ctx2.select.attr("img")
                }
                // alternatively
                http(get(imgSrc), get(imgSrc)) { case (ctx1, ctx2) =>
                    //
                }
            }
        )
        select("#form-login") {
            select("#name").setValue("John")
            select("#pass").setValue("qwerty")
            submit("#btnSubmit") {
                val imgId = select("img[src]").attr("id")
                toFile(s"C:/temp/form/${imgId}.png")
            }
        }
    }

    import com.ilyamur.scrawler._

    get ("http://www.deviantart.com") {
        select (".grid-dailydev .tt-a.thumb") {
            """.+/(.+.jpg)""".r.findFirstMatchIn(attr("data-super-full-img")).foreach { m =>
                val src = m.group(0)
                val name = m.group(1)
                get (src) {
                    writeToFile("D:/dailydev/" + name)
                }
            }
        }
    }

    get("http://www.deviantart.com", {
        //
    })

    get "http://ya.ru" - 200
        select ".block" - 3 times
        ---1
            select "img[src]" - 2 times
            ---1
                get "http://ya.ru/img/logo1.png" - 200
                    saving 1/1
                    to file "C:/temp/block1/image1.png"
            ---2
                get "http://ya.ru/img/logo2.png" - 200
                    saving 1/2
                    to file "C:/temp/block1/image2.png"
        ---2
            select "img[src]" - 2 times
            ---1
                get "http://ya.ru/img/logo3.png" - 404
            ---2
                get "http://ya.ru/img/logo4.png" - 200
                    saving 2/2
                    to file "C:/temp/block2/image2.png"
        ---3
            exception - ...

    get "http://ya.ru" - 200
        select ".block" - 3 times
        ---2
            select "img[src]" - 2 times
            ---1
                get "http://ya.ru/img/logo3.png" - 404
        ---3
            exception - ...

     */

    class Resource {

        def getInputStream: InputStream = ???
    }

    class ToOutputStreamHandler(os: OutputStream, osFuture: Future[_]) {

        def thenClose(): Unit = {
            osFuture.onComplete { _ =>
                os.close()
            }
        }
    }

    def toOutputStream(name: String, os: OutputStream)(implicit r: Resource, ec: ExecutionContext): ToOutputStreamHandler = {
        val osFuture = Future {
            IOUtils.copy(r.getInputStream, os)
        }
        new ToOutputStreamHandler(os, osFuture)
    }

    def toFile(path: String)(implicit r: Resource, ec: ExecutionContext): Unit = {
        toOutputStream(path, new FileOutputStream(path))(r, ec).thenClose()
    }

    Future.sequence(respBody).onComplete {
        case Success(value) => println(value)
        case Failure(e) => println("Error: " + e.getMessage)
    }
}
