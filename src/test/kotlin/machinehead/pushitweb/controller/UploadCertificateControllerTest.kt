package machinehead.pushitweb.controller

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import machinehead.pushitweb.constants.Constants.Companion.TEST_APP
import machinehead.pushitweb.constants.Constants.Companion.TEST_PASSWORD
import machinehead.pushitweb.entities.Application
import machinehead.pushitweb.exception.ApiError
import machinehead.pushitweb.model.CertificateUploadApiResponse
import machinehead.pushitweb.repositories.ApplicationRepository
import machinehead.pushitweb.repositories.PushUserRepository
import machinehead.pushitweb.service.CertificateService
import machinehead.pushitweb.service.JWTokenService
import machinehead.pushitweb.util.TokenUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*


@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@Import(value = [JWTokenService::class])
@MockBean(PushUserRepository::class)
@WebMvcTest(controllers = [UploadCertificateController::class, CertificateService::class])
internal class UploadCertificateControllerTest {

    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    lateinit var applicationRepository: ApplicationRepository

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun before() {
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        objectMapper
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Test
    fun `upload certificate for app name with success`() {

        val token = TokenUtil.generateTestToken()

        val certificateFile = ClassPathResource("/cert/myKeystore.p12")
        val myKeyStore = MockMultipartFile("p12", certificateFile.filename!!, "application/x-pkcs12", certificateFile.inputStream)
        val encodedCertificate = Base64.getEncoder().encodeToString(myKeyStore.bytes)

        val postMultipart = mockHttpServletRequestBuilder(myKeyStore, token, TEST_APP, TEST_PASSWORD)

        val result = mvc.perform(postMultipart)
                .andExpect(status().isOk)
                .andReturn()

        val bodyResponse = objectMapper.readValue(result.response.contentAsString, CertificateUploadApiResponse::class.java)

        assertThat(result).isNotNull
        assertThat(bodyResponse.appName).isEqualTo(TEST_APP)

        val argumentCaptor = ArgumentCaptor.forClass(Application::class.java)

        verify(applicationRepository, atLeastOnce()).save(argumentCaptor.capture())

        assertThat(TEST_APP).isEqualTo(argumentCaptor.value.appName)
        assertThat(TEST_PASSWORD).isEqualTo(argumentCaptor.value.certPass)
        assertThat(encodedCertificate).isEqualTo(argumentCaptor.value.certificate)
    }


    @Test
    fun `upload certificate for invalid app name ends with bad request`() {

        val token = TokenUtil.generateTestToken()

        val certificateFile = ClassPathResource("/cert/myKeystore.p12")
        val myKeyStore = MockMultipartFile("p12", certificateFile.filename!!, "application/x-pkcs12", certificateFile.inputStream)

        val postMultipart = mockHttpServletRequestBuilder(myKeyStore, token, "", TEST_PASSWORD)

        val result = mvc.perform(postMultipart)
                .andExpect(status().isBadRequest)
                .andReturn()

        assertThat(result).isNotNull

        val message = "The appName is blank: %s, password is blank: %s,  certificate file is empty: %s".format("".isBlank(), TEST_PASSWORD.isBlank(), myKeyStore.isEmpty)
        val bodyResponse = objectMapper.readValue(result.response.contentAsString, ApiError::class.java)

        assertThat(bodyResponse.message).isEqualTo(message)

        verify(applicationRepository, never()).save(any())
    }

    private fun mockHttpServletRequestBuilder(myKeyStore: MockMultipartFile, token: String, appName: String, password: String): MockHttpServletRequestBuilder {
        return multipart("/certificate")
                .file(myKeyStore)
                .header("Authorization", "Bearer $token")
                .header("app-name", appName)
                .header("password", password)
                .accept(APPLICATION_JSON)
    }
}