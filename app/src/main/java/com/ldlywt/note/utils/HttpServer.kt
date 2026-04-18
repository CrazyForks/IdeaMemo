package com.ldlywt.note.utils

import com.ldlywt.note.bean.NoteShowBean
import io.ktor.http.ContentType
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File
import java.net.Inet4Address
import java.net.NetworkInterface

object HttpServer {
    private var server: EmbeddedServer<*, *>? = null

    fun start(notes: List<NoteShowBean>, port: Int = 8080) {
        if (server != null) return
        val engine = embeddedServer(CIO, port = port) {
            routing {
                get("/") {
                    val html = BackUp.generateHtml(notes)
                    call.respondText(html, ContentType.Text.Html)
                }
                get("/file/{date}/{id}/{name}") {
                    val name = call.parameters["name"]
                    val attachments = notes.flatMap { it.note.attachments }
                    val attachment = attachments.find { File(it.path).name == name }
                    if (attachment != null) {
                        call.respondFile(File(attachment.path))
                    }
                }
            }
        }
        server = engine
        engine.start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 2000)
        server = null
    }

    fun isRunning(): Boolean {
        return server != null
    }

    fun getIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
