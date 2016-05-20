package com.ilyamur.scrawler

import java.util.concurrent.Executors

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.{ClientProtocolException, ResponseHandler}
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object Application extends App {

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

    def onHttpClientConnectionManagerAsync[A](maxConnections: Int)(f: HttpClientConnectionManager => Seq[Future[A]]): Seq[Future[A]] = {
        val cm = new PoolingHttpClientConnectionManager()
        cm.setMaxTotal(maxConnections)
        cm.setDefaultMaxPerRoute(maxConnections)
        sequenceResourceUseReleaseAsync(cm)(f)(_.shutdown())
    }

    def onHttpClientAsync[A](maxConnections: Int)(f: CloseableHttpClient => Seq[Future[A]]): Seq[Future[A]] = {
        onHttpClientConnectionManagerAsync(maxConnections) { cm =>
            val client = HttpClients.custom()
                    .setConnectionManager(cm)
                    .build()
            sequenceResourceUseReleaseAsync(client)(f)(_.close())
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

    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(16))

    val respBody = onHttpClientAsync(MAX_CONNECTIONS) { httpClient =>
        val a = Future {
            httpClient.execute(new HttpGet("http://ya.ru"), respHandler)
        }
        val b = Future {
            httpClient.execute(new HttpGet("http://google.ru"), respHandler)
        }
        Seq(a, b)
    }

    Future.sequence(respBody).onComplete {
        case Success(value) => println(value)
        case Failure(e) => println("Error: " + e.getMessage)
    }
}
