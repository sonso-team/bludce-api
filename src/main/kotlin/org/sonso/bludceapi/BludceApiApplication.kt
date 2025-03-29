package org.sonso.bludceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
class BludceApiApplication

fun main(args: Array<String>) {
    runApplication<BludceApiApplication>(*args)
}
