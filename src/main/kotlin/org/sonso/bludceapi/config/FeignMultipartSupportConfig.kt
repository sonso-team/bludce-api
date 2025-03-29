package org.sonso.bludceapi.config

import feign.codec.Encoder
import feign.form.spring.SpringFormEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignMultipartSupportConfig {

    @Bean
    fun feignEncoder(): Encoder {
        return SpringFormEncoder()
    }
}
