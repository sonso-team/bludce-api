package org.sonso.bludceapi

import org.sonso.bludceapi.config.properties.AuthenticationProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(
    AuthenticationProperties::class
)
class BludceApiApplication

fun main(args: Array<String>) {
    runApplication<BludceApiApplication>(*args)
}
