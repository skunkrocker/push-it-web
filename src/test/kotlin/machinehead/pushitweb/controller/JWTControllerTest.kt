package machinehead.pushitweb.controller

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import machinehead.pushitweb.constants.Constants.Companion.BEARER
import machinehead.pushitweb.constants.Constants.Companion.TEST_PASSWORD
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.model.api.JWTApiRequest
import machinehead.pushitweb.model.api.JWTApiResponse
import machinehead.pushitweb.service.JWTokenService
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@AutoConfigureMockMvc
@ExtendWith(SpringExtension::class)
@WebMvcTest(controllers = [JWTController::class])
internal class JWTControllerTest {

    private lateinit var mvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var jwTokenService: JWTokenService

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun before() {
        jwTokenService.jwtSecret = "biGSecret"

        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

        objectMapper
                .configure(SerializationFeature.WRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Test
    fun authenticate_TestUser_Success_JWTToken() {

        val result = mvc
                .perform(postServletRequest(JWTApiRequest(TEST_USER, TEST_PASSWORD)))
                .andExpect(status().isOk)
                .andReturn()

        val bodyResponse = objectMapper.readValue(result.response.contentAsString, JWTApiResponse::class.java)

        assertThat(bodyResponse).isNotNull
        assertThat(bodyResponse.accessToken).isNotNull()
        assertThat(bodyResponse.accessToken).isNotBlank()
        assertThat(bodyResponse.tokenType).isEqualTo(BEARER.trim())
    }

    @Test
    fun authenticate_InvalidUser_NoToken() {
        mvc
                .perform(postServletRequest(JWTApiRequest("unknown-user", "no password")))
                .andExpect(status().isForbidden)
                .andReturn()
    }

    @Test
    fun authenticate_EmptyUser_NoToken() {
        mvc
                .perform(postServletRequest(JWTApiRequest("", "no password")))
                .andExpect(status().isBadRequest)
                .andReturn()
    }

    @Test
    fun authenticate_EmptyPass_NoToken() {
        mvc
                .perform(postServletRequest(JWTApiRequest(TEST_USER, "")))
                .andExpect(status().isBadRequest)
                .andReturn()
    }

    private fun postServletRequest(jwtApiRequest: JWTApiRequest): MockHttpServletRequestBuilder {
        val jwtApiRequestString = objectMapper.writeValueAsString(jwtApiRequest)

        return post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jwtApiRequestString)
    }
}