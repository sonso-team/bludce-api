package org.sonso.bludceapi

import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableConfigurationProperties(
    AuthenticationProperties::class
)
@EnableFeignClients
@EnableAsync
class BludceApiApplication

fun main(args: Array<String>) {
    runApplication<BludceApiApplication>(*args)
}
