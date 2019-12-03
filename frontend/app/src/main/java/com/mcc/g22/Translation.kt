package com.mcc.g22

import android.util.Log
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.Exception


class Translation {

    companion object {
        private val KEY: String = ""
        private val httpClient: OkHttpClient = OkHttpClient()

        @JsonClass(generateAdapter = true)
        data class TranslationApiResponse(val data: Data)
        data class Data(val translations: List<TranslationResult>)
        data class TranslationResult(val translatedText: String)

        private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        private val moshiAdapter =
            moshi.adapter<TranslationApiResponse>(TranslationApiResponse::class.java)


        /**
         * Translate the text to the target language
         *
         * @param textToTranslate text to be translated
         * @param targetLanguage  iso-639-1 code of the language to which the text should be
         *                          translated
         * @param onTranslationCompleted function to be called when translation is completed
         * @param onTranslationFailed function to be called when
         */
        fun translate(textToTranslate: String,
                      targetLanguage: String,
                      onTranslationCompleted: (translation: String) -> Unit,
                      onTranslationFailed: () -> Unit) {

            val body: RequestBody =
                "{'q':'$textToTranslate' , 'target': '$targetLanguage' , 'format':'text'}"
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val r = Request.Builder()
                .url("https://translation.googleapis.com/language/translate/v2?key=$KEY")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            httpClient.newCall(r).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("MCC", e.toString())
                    onTranslationFailed()
                }

                override fun onResponse(call: Call, response: Response) = try {
                    val result = moshiAdapter.fromJson(response.body!!.string())
                    onTranslationCompleted(result!!.data.translations[0].translatedText)
                } catch (e: Exception) {
                    Log.e("MCC", "Failed parsing response: " + e.toString() + " " + response.body!!.string())
                    onTranslationFailed()
                }
            })
        }
    }
}