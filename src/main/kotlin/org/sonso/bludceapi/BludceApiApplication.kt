package org.sonso.bludceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BludceApiApplication

fun main(args: Array<String>) {
    runApplication<BludceApiApplication>(*args)
}
