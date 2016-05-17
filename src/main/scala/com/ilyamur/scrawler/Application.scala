package com.ilyamur.scrawler

import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.{ClientProtocolException, ResponseHandler}
import org.apache.http.conn.HttpClientConnectionManager
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

object Application extends App {

    private val MAX_CONNECTIONS = 16
    private val URL = "http://ya.ru"

    def getPoolingHttpClientConnectionManager(maxConnections: Int): HttpClientConnectionManager = {
        val cm = new PoolingHttpClientConnectionManager()
        cm.setMaxTotal(maxConnections)
        cm.setDefaultMaxPerRoute(maxConnections)
        cm
    }

    def getPoolingHttpClient(maxConnections: Int): CloseableHttpClient = {
        val cm = getPoolingHttpClientConnectionManager(maxConnections)
        HttpClients.custom()
                .setConnectionManager(cm)
                .build()
    }

    val httpClient = getPoolingHttpClient(MAX_CONNECTIONS)

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

    val respBody = httpClient.execute(new HttpGet(URL), respHandler)

    println(respBody)
    println("")
    println("ok")
}
