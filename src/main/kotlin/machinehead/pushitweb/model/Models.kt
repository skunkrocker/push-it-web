package machinehead.pushitweb.model

import machinehead.pushitweb.constants.Constants

//JWT Controller API
class JWTApiRequest(var userName: String, var password: String)
class JWTApiResponse(val tokenType: String = Constants.BEARER.trim(), var accessToken: String = "")

//Certificate Controller API
class CertificateUploadApiResponse(val appName: String)

//Apple Push Controller
data class APNSResponseApi(val reason: String)
data class PlatformResponseApi(val status: Int, val apns: APNSResponseApi)
data class PushResultApi(val token: String, val response: PlatformResponseApi)
