package machinehead.pushitweb.model.api

class JWTApiResponse(var accessToken: String)

class JWTApiRequest(var userName: String, var password: String)
