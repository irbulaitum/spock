import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.config.SSLConfig

interface httpConf {

    def config = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                    .setParam("http.socket.timeout",5000)
                    .setParam("http.connection.timeout", 5000))
            /*.sslConfig(SSLConfig.sslConfig()
                    .keyStore("./src/test/resources/ssl/certificate.p12", "password")
                    .keystoreType("PKCS12")
                    .relaxedHTTPSValidation())*/
}