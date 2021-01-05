package machinehead.pushitweb.controller

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRAP_ROOT_VALUE
import machinehead.pushitweb.constants.Constants
import machinehead.pushitweb.constants.Constants.Companion.BEARER
import machinehead.pushitweb.constants.Constants.Companion.TEST_PASSWORD
import machinehead.pushitweb.constants.Constants.Companion.TEST_USER
import machinehead.pushitweb.entity.PushUser
import machinehead.pushitweb.model.JWTApiRequest
import machinehead.pushitweb.model.JWTApiResponse
import machinehead.pushitweb.repository.PushUserRepository
import machinehead.pushitweb.service.JWTokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    @MockBean
    lateinit var pushUserRepository: PushUserRepository

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    @BeforeEach
    fun before() {
        jwTokenService.jwtSecret = "biGSecret"

        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build()

        objectMapper
                .configure(WRAP_ROOT_VALUE, false)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    @Test
    fun `authentication with valid user returns the JWT-Token`() {

        given(pushUserRepository.findByUserName(TEST_USER))
                .willReturn(PushUser(Constants.TEST_ROLE, TEST_USER, TEST_PASSWORD, 1))

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
    fun `authentication invalid user and fail with forbidden`() {
        mvc
                .perform(postServletRequest(JWTApiRequest("unknown-user", "no password")))
                .andExpect(status().isForbidden)
                .andReturn()
    }

    @Test
    fun `authentication with empty user and return bad request`() {
        mvc
                .perform(postServletRequest(JWTApiRequest("", "no password")))
                .andExpect(status().isBadRequest)
                .andReturn()
    }

    @Test
    fun `authentication with password is empty and returns bad request`() {
        mvc
                .perform(postServletRequest(JWTApiRequest(TEST_USER, "")))
                .andExpect(status().isBadRequest)
                .andReturn()
    }

    private fun postServletRequest(jwtApiRequest: JWTApiRequest): MockHttpServletRequestBuilder {
        val jwtApiRequestString = objectMapper.writeValueAsString(jwtApiRequest)

        return post("/auth")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(jwtApiRequestString)
    }
}