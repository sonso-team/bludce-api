package org.sonso.bludceapi.dto.response.ocr

import com.fasterxml.jackson.annotation.JsonProperty

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
    val width: Double
)
