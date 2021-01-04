package machinehead.pushitweb.entities

import javax.persistence.*

@Entity
@Table(name = "push_user", schema = "public")
open class PushUser(
    open var role: String,
    open var userName: String,
    open var password: String,
    @Id @GeneratedValue open var id: Long? = null
)

@Entity
@Table(name = "application", schema = "public")
open class Application(
    open var appName: String,
    open var certPass: String,
    open var certificate: String,
    @Id @GeneratedValue open var id: Long? = null
)