import groovy.text.StreamingTemplateEngine
import io.restassured.builder.RequestSpecBuilder
import spock.lang.Ignore
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static io.restassured.RestAssured.*
import groovy.xml.XmlParser


class srvCalcTest extends Specification{
    @Shared
    def sharedVar = 'Эта переменная доступна во всех тестах спецификации'
    //Настроим подключение
    //Спецификация включающая параметры SSL, базовый uri и т.д.
    def requestSpec = new RequestSpecBuilder()
            .setConfig(httpConf.config) //это конфиг с параметрами подключения
            .setBaseUri("http://127.0.0.1:8080") //Бызовый URI, который будет использоваться для запросов, по умолчанию rtpg
            .addHeaders("myHeader":"chpok", "Content-Type":"application/xml; charset=UTF-8")
            .build()
    //@Ignore
    @Retry
    def "Это тестовый метод"(){
        given: //Дано (голая баба лезет в окно)
        def msgParams = [
                rquid: UUID.randomUUID().toString().replace("-",""),
                rqtm: LocalDateTime.now().format("yyyy-MM-dd'T'HH:mm:ss+03:00"),
                a: a,
                b: b
        ]

        when: //Когда выполняем действие
        def response = given().spec(requestSpec)
                .when()
                .body(new StreamingTemplateEngine().createTemplate(new File("src/test/resources/srvCalcRq.xml")).make(msgParams).toString())
                .post("/srvCalc")
                .then().log().all()
                .statusCode(200)
        def CalcRs = new XmlParser().parseText(response.extract().response().asString())

        then: //Тогда ожидаем следующий результат
        verifyAll {
            assert CalcRs.rquid.text() != ''
            assert CalcRs.rqtm.text() != ''
            assert CalcRs.status.text() == 'OK'
            assert CalcRs.statusDesc.text() == 'Успешно'
            assert CalcRs.result.text().toInteger() == a + b
            assert CalcRs.result.text().toInteger() == result
        }

        where: //Параметры теста
        a|b|result
        1|2|3
        3|3|6
        4|5|9
    }
}