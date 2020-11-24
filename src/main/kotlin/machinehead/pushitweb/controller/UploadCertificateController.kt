package machinehead.pushitweb.controller

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.multipart.MultipartFile
import machinehead.pushitweb.service.CertificateService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestHeader
import machinehead.pushitweb.model.CertificateUploadApiResponse

@Controller
open class UploadCertificateController(private val certificateService: CertificateService) {

    private val LOGGER: Logger = LoggerFactory.getLogger(UploadCertificateController::class.java)

    @PostMapping(value = ["/certificate"])
    fun uploadCertificate(
            @RequestHeader("app-name") appName: String,
            @RequestHeader("password") certificatePassword: String,
            @RequestParam("p12") certificateFile: MultipartFile,
            @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<CertificateUploadApiResponse> {

        val theAppName = certificateService.saveCertificate(appName, certificatePassword, certificateFile)

        LOGGER.debug("The certificate was saved for the app name: {}", theAppName)

        return ResponseEntity.ok(CertificateUploadApiResponse(theAppName))
    }
}
