package org.sonso.bludceapi.client

import org.sonso.bludceapi.config.FeignMultipartSupportConfig
import org.sonso.bludceapi.dto.response.ocr.OcrServiceClientResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@FeignClient(
    name = "ocrClient",
    url = "https://api.ocr.space",
    configuration = [FeignMultipartSupportConfig::class]
)
interface OcrServiceClient {

    @PostMapping("/parse/image", consumes = ["multipart/form-data"])
    fun getTextFromImage(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("language") language: String = "rus",
        @RequestPart("isTable") isTable: String = "true",
        @RequestPart("OCREngine") oCREngine: String = "5"
    ): OcrServiceClientResponse
}
