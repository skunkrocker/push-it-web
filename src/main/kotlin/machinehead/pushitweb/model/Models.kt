package machinehead.pushitweb.model

import machinehead.pushitweb.constants.Constants


class JWTApiRequest(var userName: String, var password: String)
class JWTApiResponse(val tokenType: String = Constants.BEARER.trim(), var accessToken: String = "")

class CertificateUploadApiResponse(val appName: String)

data class APNSResponseApi(val reason: String)
data class PlatformResponseApi(val status: Int, val apns: APNSResponseApi)
data class PushResultApi(val token: String, val response: PlatformResponseApi)
