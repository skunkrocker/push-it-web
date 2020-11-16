package machinehead.pushitweb.model.api

import org.springframework.web.multipart.MultipartFile

class UploadApiRequest(val appName: String, val certPass: String, val multipartFile: MultipartFile) {
}