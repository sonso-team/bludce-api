package org.sonso.bludceapi.dto.response.ocr

import com.fasterxml.jackson.annotation.JsonProperty

data class OcrLineResponse(
    @JsonProperty("LineText")
    val lineText: String,

    @JsonProperty("Words")
    val words: List<OcrWordResponse>,

    @JsonProperty("MaxHeight")
    val maxHeight: Double,

    @JsonProperty("MinTop")
    val minTop: Double
)
