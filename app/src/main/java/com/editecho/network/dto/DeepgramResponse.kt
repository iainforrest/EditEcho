package com.editecho.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data classes for parsing Deepgram's streaming API JSON responses.
 * Based on the official Deepgram documentation response schema.
 */

@Serializable
data class DeepgramResponse(
    @SerialName("type") val type: String,
    @SerialName("channel_index") val channelIndex: List<Int>? = null,
    @SerialName("duration") val duration: Double? = null,
    @SerialName("start") val start: Double? = null,
    @SerialName("is_final") val isFinal: Boolean? = null,
    @SerialName("speech_final") val speechFinal: Boolean? = null,
    @SerialName("channel") val channel: DeepgramChannel? = null,
    @SerialName("metadata") val metadata: DeepgramMetadata? = null,
    @SerialName("from_finalize") val fromFinalize: Boolean? = null
)

@Serializable
data class DeepgramChannel(
    @SerialName("alternatives") val alternatives: List<DeepgramAlternative> = emptyList(),
    @SerialName("search") val search: List<DeepgramSearch>? = null
)

@Serializable
data class DeepgramAlternative(
    @SerialName("transcript") val transcript: String,
    @SerialName("confidence") val confidence: Double? = null,
    @SerialName("words") val words: List<DeepgramWord>? = null
)

@Serializable
data class DeepgramWord(
    @SerialName("word") val word: String,
    @SerialName("start") val start: Double,
    @SerialName("end") val end: Double,
    @SerialName("confidence") val confidence: Double,
    @SerialName("punctuated_word") val punctuatedWord: String? = null
)

@Serializable
data class DeepgramSearch(
    @SerialName("query") val query: String,
    @SerialName("hits") val hits: List<DeepgramSearchHit>
)

@Serializable
data class DeepgramSearchHit(
    @SerialName("confidence") val confidence: Double,
    @SerialName("start") val start: Double,
    @SerialName("end") val end: Double,
    @SerialName("snippet") val snippet: String
)

@Serializable
data class DeepgramMetadata(
    @SerialName("transaction_key") val transactionKey: String? = null,
    @SerialName("request_id") val requestId: String? = null,
    @SerialName("sha256") val sha256: String? = null,
    @SerialName("created") val created: String? = null,
    @SerialName("duration") val duration: Double? = null,
    @SerialName("channels") val channels: Int? = null,
    @SerialName("models") val models: List<String>? = null,
    @SerialName("model_info") val modelInfo: DeepgramModelInfo? = null,
    @SerialName("model_uuid") val modelUuid: String? = null
)

@Serializable
data class DeepgramModelInfo(
    @SerialName("name") val name: String,
    @SerialName("version") val version: String,
    @SerialName("arch") val arch: String
)

/**
 * Utility extensions for working with Deepgram responses
 */
fun DeepgramResponse.getTranscript(): String {
    return channel?.alternatives?.firstOrNull()?.transcript ?: ""
}

fun DeepgramResponse.getConfidence(): Double {
    return channel?.alternatives?.firstOrNull()?.confidence ?: 0.0
}

fun DeepgramResponse.isTranscriptionResult(): Boolean {
    return type == "Results"
}

fun DeepgramResponse.isInterimResult(): Boolean {
    return isFinal == false
}

fun DeepgramResponse.isFinalResult(): Boolean {
    return isFinal == true
}

fun DeepgramResponse.isSpeechFinalResult(): Boolean {
    return speechFinal == true
} 