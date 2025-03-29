package org.sonso.bludceapi.dto.response.ocr

import com.fasterxml.jackson.annotation.JsonProperty

data class TextOverlayResponse(
    @JsonProperty("Lines")
    val lines: List<OcrLineResponse>,

    @JsonProperty("HasOverlay")
    val hasOverlay: Boolean
)
