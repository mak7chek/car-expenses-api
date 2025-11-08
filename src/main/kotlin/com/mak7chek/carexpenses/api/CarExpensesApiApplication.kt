package com.mak7chek.carexpenses.api

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
class CarExpensesApiApplication

fun main(args: Array<String>) {
	runApplication<CarExpensesApiApplication>(*args)
}
