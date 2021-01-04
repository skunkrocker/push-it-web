package machinehead.pushitweb.service

import java.util.*
import java.lang.IllegalArgumentException
import org.springframework.stereotype.Service
import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.logger
import org.springframework.web.multipart.MultipartFile
import machinehead.pushitweb.repositories.ApplicationRepository
import org.springframework.dao.DataIntegrityViolationException

@Service
class CertificateService(private val applicationRepository: ApplicationRepository) {

    private val logger by logger()

    fun saveCertificate(appName: String, certificatePassword: String, certificateFile: MultipartFile): String {

        validateArguments(appName, certificatePassword, certificateFile)

        val encodedCertificate = Base64.getEncoder().encodeToString(certificateFile.bytes)

        logger.debug("The certificate was encoded: {}", encodedCertificate.substring(0, 10))

        saveApplication(appName, certificatePassword, encodedCertificate)

        return appName
    }

    private fun saveApplication(appName: String, certificatePassword: String, encodedCertificate: String) =
        try {
            applicationRepository.save(Application(appName, certificatePassword, encodedCertificate))
            logger.debug("The certificate with the name: {} was stored to the database.", appName)

        } catch (e: DataIntegrityViolationException) {
            val message = "The app with the name: %s already exists.".format(appName)
            logger.error(message, e)
            throw IllegalArgumentException(message)
        }

    private fun validateArguments(appName: String, certificatePassword: String, certificateFile: MultipartFile) {

        if (appName.isBlank() or certificatePassword.isBlank() or certificateFile.isEmpty) {
            val message = "The appName is blank: %s, password is blank: %s,  certificate file is empty: %s".format(
                appName.isBlank(),
                certificatePassword.isBlank(),
                certificateFile.isEmpty
            )
            logger.error("Invalid certificate information.")
            logger.error(message)

            throw IllegalArgumentException(message)
        }

        if (certificateFile.contentType != "application/x-pkcs12") {
            val message =
                "The content type of the file is not supported: %s. At the moment only application/x-pkcs12 certificates are supported."
                    .format(certificateFile.contentType)

            logger.error(message)
            throw IllegalArgumentException(message)
        }
    }
}