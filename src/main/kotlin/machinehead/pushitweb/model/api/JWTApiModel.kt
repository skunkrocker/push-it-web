package machinehead.pushitweb.model.api

import machinehead.pushitweb.constants.Constants

class JWTApiResponse(val tokenType: String = Constants.BEARER.trim(), var accessToken: String)

class JWTApiRequest(var userName: String, var password: String)
