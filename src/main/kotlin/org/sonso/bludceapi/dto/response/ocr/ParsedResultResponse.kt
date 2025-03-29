package org.sonso.bludceapi.dto.response.ocr

import com.fasterxml.jackson.annotation.JsonProperty

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
    val errorDetails: String
)
