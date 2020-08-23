package machinehead.pushitweb

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class PushItWebApplication

fun main(args: Array<String>) {
    runApplication<PushItWebApplication>(*args)
}
