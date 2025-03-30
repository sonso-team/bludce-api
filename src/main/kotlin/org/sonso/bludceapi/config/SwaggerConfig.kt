package org.sonso.bludceapi.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info

@OpenAPIDefinition(
    info = Info(
        title = "Блюдце",
        description = """          
            Сервис по упрощению деления счета среди компании из нескольких человек
            
            Выполнено в рамках чемпионата ProЦифру 2025""",
        version = "1.0.0"
    )
)
class SwaggerConfig
