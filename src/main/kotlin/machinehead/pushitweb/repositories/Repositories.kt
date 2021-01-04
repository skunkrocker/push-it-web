package machinehead.pushitweb.repositories

import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.entities.PushUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

import javax.transaction.Transactional;

@Repository
@Transactional
interface PushUserRepository : JpaRepository<PushUser, Long> {
    fun findByUserName(userName: String): PushUser?
}

@Repository
@Transactional
interface ApplicationRepository : JpaRepository<Application, Long> {

    fun findByAppName(appName: String): Application?
}