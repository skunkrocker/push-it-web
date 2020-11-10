package machinehead.pushitweb

import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class PushItWebApplication

fun main(args: Array<String>) {
    runApplication<PushItWebApplication>(*args)
}
