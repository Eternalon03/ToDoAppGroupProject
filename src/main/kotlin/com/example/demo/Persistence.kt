package com.example.demo

import java.io.OutputStream
import java.io.InputStream
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.nio.file.Path
import java.nio.file.Paths
import java.lang.Exception
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

object Persistence{
    inline fun <reified T> load(input: InputStream): T? {
        return try{
            Json.decodeFromString(String(input.readAllBytes(), Charsets.UTF_8))
        }catch(ex: Exception){ System.err.println(ex) ; null }
    }
    inline fun <reified T> load(inputPath: Path): T? {
        return try{
            val inputFile = inputPath.toFile()
            inputFile.createNewFile() // creates a new file in case one does not exist
            load(inputFile.inputStream())
        }catch(ex: Exception){ System.err.println(ex) ; null }
    }
    inline fun <reified T> load(inputPathString: String): T? {
        return try{
            load(Paths.get(inputPathString))
        }catch(ex: Exception){ System.err.println(ex) ; null }
    }
    inline fun <reified T> save(value: T, output: OutputStream) {
        try {
            output.write(Json.encodeToString(value).toByteArray(Charsets.UTF_8))
        }catch(ex: Exception){ System.err.println(ex) }
    }
    inline fun <reified T> save(value: T, outputPath: Path) {
        try{
            val outputFile = outputPath.toFile()
            outputFile.createNewFile() // creates a new file in case one does not exist
            save(value, outputFile.outputStream())
        }catch(ex: Exception){ System.err.println(ex) }
    }
    inline fun <reified T> save(value: T, outputPathString: String) {
        try{
            save(value, Paths.get(outputPathString))
        }catch(ex: Exception){ System.err.println(ex) }
    }
    object Cloud {
        inline fun <reified T> get(inputURL: String): T? {
            return try {
                val client = HttpClient.newBuilder().build()
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(inputURL))
                    .GET()
                    .build()
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                Json.decodeFromString(response.body())
            }catch(ex: Exception){ System.err.println(ex) ; null }
        }
        inline fun <reified T> put(value: T, outputURL: String) {
            try {
                val contents = Json.encodeToString(value)
                val client = OkHttpClient().newBuilder()
                    .build()
                val mediaType = MediaType.parse("text/plain")
                val body = RequestBody.create(mediaType, contents)
                val request = Request.Builder()
                    .url(outputURL)
                    .method("PUT", body)
                    .addHeader("x-ms-date", "1668708009")
                    .addHeader("x-ms-version", "2019-12-12") // .addHeader("Content-Length", "13")
                    .addHeader("x-ms-blob-type", "BlockBlob")
                    .addHeader("x-ms-meta-m1", "v1")
                    .addHeader("x-ms-meta-m2", "v2")
                    .addHeader("Content-Type", "text/plain")
                    .build()
                val response = client.newCall(request).execute()
            }catch(ex: Exception){ System.err.println(ex) }
        }
    }
}
