package org.sonso.bludceapi.dto.client

import com.fasterxml.jackson.annotation.JsonProperty

data class OcrServiceClientResponse(
    @JsonProperty("ParsedResults")
    val parsedResults: List<ParsedResultResponse>? = null,

    @JsonProperty("OCRExitCode")
    val ocrExitCode: Int,

    @JsonProperty("IsErroredOnProcessing")
    val isErroredOnProcessing: Boolean,

    @JsonProperty("ProcessingTimeInMilliseconds")
    val processingTimeInMilliseconds: String,

    @JsonProperty("SearchablePDFURL")
    val searchablePDFURL: String? = null,

    @JsonProperty("ErrorMessage")
    val errorMessage: List<String>? = null,

    @JsonProperty("ErrorDetails")
    val errorDetails: String? = null
) {

    data class TextOverlayResponse(
        @JsonProperty("Lines")
        val lines: List<OcrLineResponse>,

        @JsonProperty("HasOverlay")
        val hasOverlay: Boolean
    )

    data class ParsedResultResponse(
        @JsonProperty("TextOverlay")
        val textOverlay: TextOverlayResponse,

        @JsonProperty("TextOrientation")
        val textOrientation: String,

        @JsonProperty("FileParseExitCode")
        val fileParseExitCode: Int,

        @JsonProperty("ParsedText")
        val parsedText: String,

        @JsonProperty("ErrorMessage")
        val errorMessage: String,

        @JsonProperty("ErrorDetails")
        val errorDetails: String,
    )

    data class OcrLineResponse(
        @JsonProperty("LineText")
        val lineText: String,

        @JsonProperty("Words")
        val words: List<OcrWordResponse>,

        @JsonProperty("MaxHeight")
        val maxHeight: Double,

        @JsonProperty("MinTop")
        val minTop: Double,
    )

    data class OcrWordResponse(
        @JsonProperty("WordText")
        val wordText: String,

        @JsonProperty("Left")
        val left: Double,

        @JsonProperty("Top")
        val top: Double,

        @JsonProperty("Height")
        val height: Double,

        @JsonProperty("Width")
        val width: Double,
    )
}
