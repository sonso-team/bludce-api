package org.sonso.bludceapi.dto.response.ocr

import com.fasterxml.jackson.annotation.JsonProperty

data class OcrServiceClientResponse(
    @JsonProperty("ParsedResults")
    val parsedResults: List<ParsedResultResponse>,

    @JsonProperty("OCRExitCode")
    val ocrExitCode: Int,

    @JsonProperty("IsErroredOnProcessing")
    val isErroredOnProcessing: Boolean,

    @JsonProperty("ProcessingTimeInMilliseconds")
    val processingTimeInMilliseconds: String,

    @JsonProperty("SearchablePDFURL")
    val searchablePDFURL: String
)
