package machinehead.pushitweb.service

import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.repositories.ApplicationRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class CertificateService(private val applicationRepository: ApplicationRepository) {

    fun saveCertificate(appName: String, certificatePassword: String, certificateFile: MultipartFile) {
        if (appName.isBlank() or certificatePassword.isBlank() or certificateFile.isEmpty) {
            //TODO throw exception
        }

        val encodeToString = Base64.getEncoder().encodeToString(certificateFile.bytes)

        applicationRepository.save(Application(appName, certificatePassword, encodeToString))

        applicationRepository.findAll()
                .forEach { app ->
                    println(app)
                }
    }
}