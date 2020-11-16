package machinehead.pushitweb.service

import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.repositories.ApplicationRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class CertificateService(private val applicationRepository: ApplicationRepository) {

    private val LOGGER: Logger = LoggerFactory.getLogger(CertificateService::class.java)

    fun saveCertificate(appName: String, certificatePassword: String, certificateFile: MultipartFile): String {
        if (appName.isBlank() or certificatePassword.isBlank() or certificateFile.isEmpty) {
            LOGGER.error("Invalid certificate information.")
            LOGGER.error("The appName: {}, password: {},  certificate file: {}", appName.isBlank(), certificatePassword.isBlank(), certificateFile.isEmpty)

            //TODO throw exception
        }

        val encodedCertificate = Base64.getEncoder().encodeToString(certificateFile.bytes)

        LOGGER.debug("The certificate was encoded: {}", encodedCertificate.substring(0, 10))

        applicationRepository.save(Application(appName, certificatePassword, encodedCertificate))

        LOGGER.debug("The certificate with the name: {} was stored to the database.", appName)

        return appName
    }
}