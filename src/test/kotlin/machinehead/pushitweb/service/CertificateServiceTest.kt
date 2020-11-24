package machinehead.pushitweb.service

import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.repositories.ApplicationRepository
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.core.io.ClassPathResource
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.mock.web.MockMultipartFile
import java.io.InputStream
import java.lang.IllegalArgumentException
import java.util.*

@SpringBootTest(classes = [CertificateService::class])
class CertificateServiceTest {

    @Autowired
    lateinit var certificateService: CertificateService

    @MockBean
    lateinit var applicationRepository: ApplicationRepository

    @Test
    fun `save a certificate based on multipart file`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "application/x-pkcs12", certificateFile.inputStream)

        val testPass = "password"
        val testApp = "test-app-name"
        val encodedCertificate = Base64.getEncoder().encodeToString(myKeyStore.bytes)

        certificateService.saveCertificate(testApp, testPass, myKeyStore)

        val argumentCaptor = ArgumentCaptor.forClass(Application::class.java)

        verify(applicationRepository, atLeastOnce()).save(argumentCaptor.capture())

        assertThat(testApp).isEqualTo(argumentCaptor.value.appName)
        assertThat(testPass).isEqualTo(argumentCaptor.value.certPass)
        assertThat(encodedCertificate).isEqualTo(argumentCaptor.value.certificate)
    }

    @Test
    fun `don't accept certificate with invalid content type`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "wrong/content-type", certificateFile.inputStream)

        val message = "The content type of the file is not supported: %s. At the moment only application/x-pkcs12 certificates are supported."
                .format(myKeyStore.contentType)

        Assertions.assertThatThrownBy {
            certificateService.saveCertificate("test-app", "password", myKeyStore)
        }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(message)

    }

    @Test
    fun `don't accept certificate with invalid app name`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "wrong/content-type", certificateFile.inputStream)

        val appName = ""
        val password = "password"

        val message = "The appName is blank: %s, password is blank: %s,  certificate file is empty: %s".format(appName.isBlank(), password.isBlank(), myKeyStore.isEmpty)


        Assertions.assertThatThrownBy {
            certificateService.saveCertificate(appName, password, myKeyStore)
        }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(message)
    }

    @Test
    fun `don't accept certificate with invalid password`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "wrong/content-type", certificateFile.inputStream)

        val appName = "test-app"
        val password = ""

        val message = "The appName is blank: %s, password is blank: %s,  certificate file is empty: %s".format(appName.isBlank(), password.isBlank(), myKeyStore.isEmpty)


        Assertions.assertThatThrownBy {
            certificateService.saveCertificate(appName, password, myKeyStore)
        }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(message)
    }

    @Test
    fun `don't accept certificate with invalid file content`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "wrong/content-type", InputStream.nullInputStream())

        val appName = "test-app"
        val password = "password"

        val message = "The appName is blank: %s, password is blank: %s,  certificate file is empty: %s".format(appName.isBlank(), password.isBlank(), myKeyStore.isEmpty)


        Assertions.assertThatThrownBy {
            certificateService.saveCertificate(appName, password, myKeyStore)
        }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(message)
    }


    @Test
    fun `don't accept certificate with app name already in existence`() {
        val certificateFile = ClassPathResource("/cert/myKeystore.p12");
        val myKeyStore = MockMultipartFile(certificateFile.filename!!, certificateFile.filename!!, "application/x-pkcs12", certificateFile.inputStream)

        val appName = "test-app"
        val password = "password"

        doThrow(DataIntegrityViolationException("some message")).`when`(applicationRepository).save(any())

        val message = "The app with the name: %s already exists.".format(appName)

        Assertions.assertThatThrownBy {
            certificateService.saveCertificate(appName, password, myKeyStore)
        }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage(message)
    }
}