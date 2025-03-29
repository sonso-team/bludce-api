package org.sonso.bludceapi.client

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class OcrApiKeyInterceptor(
    @Value("\${ocr.api.key}") private val apiKey: String
) : RequestInterceptor {

    override fun apply(template: RequestTemplate) {
        template.header("apikey", apiKey)
    }
}
