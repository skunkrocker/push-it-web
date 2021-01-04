package machinehead.pushitweb

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.runApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class PushItWebApplication

fun main(args: Array<String>) {
    runApplication<PushItWebApplication>(*args)
}

fun <T : Any> T.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(getClassName(this.javaClass)) }
}

fun <C : Any> getClassName(clazz: Class<C>): String {
    return clazz.name.removeSuffix("\$Companion")
}