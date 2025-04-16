package org.sonso.bludceapi.client

import org.slf4j.LoggerFactory
import org.sonso.bludceapi.dto.client.OcrServiceClientResponse
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Component
class OcrServiceClientFallbackFactory : FallbackFactory<OcrServiceClient> {
    override fun create(cause: Throwable): OcrServiceClient {
        return object : OcrServiceClient {
            private val log = LoggerFactory.getLogger(this::class.java)

            override fun getTextFromImage(
                @RequestPart(value = "file") file: MultipartFile,
                @RequestPart(value = "language") language: String,
                @RequestPart(value = "isTable") isTable: String,
                @RequestPart(value = "OCREngine") oCREngine: String
            ): OcrServiceClientResponse {
                log.error("Fallback: OcrService is down. Returning fallback response.", cause)
                throw RuntimeException(
                    "Fallback: OCR сервис временное недоступен." +
                        " Пожалуйста повторите позже."
                )
            }
        }
    }
}
