package machinehead.pushitweb.controller

import machinehead.pushitweb.service.CertificateService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class UploadCertificateController(val certificateService: CertificateService) {

    private val LOGGER: Logger = LoggerFactory.getLogger(UploadCertificateController::class.java)

    @PostMapping(value = ["upload/certificate"])
    fun uploadCertificate(
            @RequestParam("App name") appName: String,
            @RequestParam("Certificate password") certificatePassword: String,
            @RequestParam("P12 certificate file") certificateFile: MultipartFile,
            @RequestHeader("Authorization") authorization: String
    ): ResponseEntity<Void> {

        certificateService.saveCertificate(appName, certificatePassword, certificateFile)

        return ResponseEntity.ok().build()
    }
}
