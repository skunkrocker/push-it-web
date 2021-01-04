package machinehead.pushitweb.util

import machinehead.pushitweb.model.*
import machinehead.pushitweb.service.Stage


class TestData {
    companion object {

        const val TEST_URL_PROPERTY = "localhost.url"

        const val APP_NAME = "test-app"

        const val BODY_VALUE = "Hello World"
        const val SUBTITLE_VALUE = "Cool Subtitle"

        const val APNS_TOPIC_KEY = "apns-topic"
        const val APNS_TOPIC_VALUE = "org.machinehead.app"

        const val CUSTOM_PROPERTY_KEY = "custom-property"
        const val CUSTOM_PROPERTY_VALUE = "hello custom"

        const val CUSTOM_PROPERTY_KEY2 = "blow-up"
        const val CUSTOM_PROPERTY_VALUE2 = true

        const val TOKEN: String = "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55"
        const val BAD_DEVICE_TOKEN: String = "3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a54"

        fun `get test payload`(token: String, theHeaders: HashMap<String, Any>): Payload {

            val notification = Notification(Aps(Alert(body = BODY_VALUE, subtitle = SUBTITLE_VALUE)))

            return Payload(
                notification = notification, headers = theHeaders, custom = hashMapOf(
                    CUSTOM_PROPERTY_KEY to CUSTOM_PROPERTY_VALUE,
                    CUSTOM_PROPERTY_KEY2 to CUSTOM_PROPERTY_VALUE2
                ), stage = Stage.DEVELOPMENT, tokens = mutableListOf(token),
                appName = APP_NAME
            )
        }
    }
}
