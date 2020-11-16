package machinehead.pushitweb.model

import machinehead.pushitweb.constants.Constants


class JWTApiRequest(var userName: String, var password: String)
class JWTApiResponse(val tokenType: String = Constants.BEARER.trim(), var accessToken: String = "")

class CertificateUploadApiResponse(val appName: String)
