import groovy.text.StreamingTemplateEngine
import io.restassured.builder.RequestSpecBuilder
import spock.lang.Ignore
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static io.restassured.RestAssured.*
import groovy.xml.XmlParser
import groovy.sql.Sql


//@Stepwise
class srvCalcTest extends Specification{
    @Shared
    def sharedVar = 'Эта переменная доступна во всех тестах спецификации'
    //Настроим подключение
    //Спецификация включающая параметры SSL, базовый uri и т.д.
    def requestSpec = new RequestSpecBuilder()
            .setConfig(httpConf.config) //это конфиг с параметрами подключения
            .setBaseUri("http://127.0.0.1:8083") //Бызовый URI, который будет использоваться для запросов
            .addHeaders("myHeader":"chpok", "Content-Type":"application/xml; charset=UTF-8")
            .build()
    @Shared
    def sql = Sql.newInstance("jdbc:h2:mem:;DATABASE_TO_UPPER=false;", "org.h2.Driver")
            .with {
                it.execute("""create table confAB (
                                RqUID VARCHAR primary key, a VARCHAR, b VARCHAR, result VARCHAR
                                )""")
                it
            }


    //@Ignore
    //@Retry(count=3)
    def "Это тестовый метод"(){
        given: //Дано
        def msgParams = [
                rquid: UUID.randomUUID().toString().replace("-",""),
                rqtm: LocalDateTime.now().format("yyyy-MM-dd'T'HH:mm:ss+03:00"),
                a: a,
                b: b
        ]
        println "$a"

        when: //Когда выполняем действие
        def response = given().spec(requestSpec)
                .when()
                .body(new StreamingTemplateEngine().createTemplate(new File("src/test/resources/srvCalcRq.xml")).make(msgParams).toString())
                .post("/srvCalc")
                .then()//.log().all()
                .statusCode(200)
        def CalcRs = new XmlParser().parseText(response.extract().response().asString())
        sql.execute("""insert into confAB
                    (RqUID, a, b, result)
                    values (${CalcRs.rquid.text()}, $a, $b, ${CalcRs.result.text()}
                    )""")

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
        5|6|11
        6|7|13
    }

    //@Ignore
    def "Это еще один тестовый метод"(){
        given:
        def msgParams = [
                rquid: UUID.randomUUID().toString().replace("-",""),
                rqtm: LocalDateTime.now().format("yyyy-MM-dd'T'HH:mm:ss+03:00"),
                a: a,
                b: b
        ]

        expect: //Здесь мы объединили when - then
        def response = given().spec(requestSpec)
                .when()
                .body(new StreamingTemplateEngine().createTemplate(new File("src/test/resources/srvCalcRq.xml")).make(msgParams).toString())
                .post("/srvCalc")
                .then()//.log().all()
                .statusCode(200)
        def CalcRs = new XmlParser().parseText(response.extract().response().asString())

        verifyAll {
            assert CalcRs.rquid.text() != ''
            assert CalcRs.rqtm.text() != ''
            assert CalcRs.status.text() == 'OK'
            assert CalcRs.statusDesc.text() == 'Успешно'
            assert CalcRs.result.text().toInteger() == a + b
            //assert CalcRs.result.text().toInteger() == result
        }

        where: //Параметры теста
        a << [1,2,3,4,5,6,7,8,9,0]
        b << [0,9,8,7,6,5,4,3,2,1]
    }


    static def configFile = new XmlParser().parse("src/test/resources/testConfigurations.xml") //Для кейса ниже нужен Конфигурационный файл
    //@Retry(count = 1000)
    //@Ignore
    def "Это метод который берет значения из файла конфигураций"(){
        given:
        def msgParams = [
                rquid: UUID.randomUUID().toString().replace("-",""),
                rqtm: LocalDateTime.now().format("yyyy-MM-dd'T'HH:mm:ss+03:00"),
                a: a,
                b: b
        ]
        println "Значения a: $a b: $b"

        expect: //Здесь мы объединили when - then
        def response = given().spec(requestSpec)
                .when()
                .body(new StreamingTemplateEngine().createTemplate(new File("src/test/resources/srvCalcRq.xml")).make(msgParams).toString())
                .post("/srvCalc")
                .then()//.log().all()
                .statusCode(200)
        def CalcRs = new XmlParser().parseText(response.extract().response().asString())

        verifyAll {
            assert CalcRs.rquid.text() != ''
            assert CalcRs.rqtm.text() != ''
            assert CalcRs.status.text() == 'OK'
            assert CalcRs.statusDesc.text() == 'Успешно'
            assert CalcRs.result.text().toInteger() == a + b
            //assert CalcRs.result.text().toInteger() == result
        }

        where: //Параметры теста
        a << configFile.config.each{}.collect{it.a[0] != null ? it.a.text().toInteger() : 0}
        b << configFile.config.each{}.collect{it.b[0] != null ? it.b.text().toInteger() : 0}
    }


    def "Это метод который берет значения из базы данных"(){
        given:
        def msgParams = [
                rquid: UUID.randomUUID().toString().replace("-",""),
                rqtm: LocalDateTime.now().format("yyyy-MM-dd'T'HH:mm:ss+03:00"),
                a: a,
                b: b
        ]
        println "Значения a: $a b: $b"

        expect: //Здесь мы объединили when - then
        def response = given().spec(requestSpec)
                .when()
                .body(new StreamingTemplateEngine().createTemplate(new File("src/test/resources/srvCalcRq.xml")).make(msgParams).toString())
                .post("/srvCalc")
                .then()//.log().all()
                .statusCode(200)
        def CalcRs = new XmlParser().parseText(response.extract().response().asString())

        verifyAll {
            assert CalcRs.rquid.text() != ''
            assert CalcRs.rqtm.text() != ''
            assert CalcRs.status.text() == 'OK'
            assert CalcRs.statusDesc.text() == 'Успешно'
            assert CalcRs.result.text().toInteger() == a.toInteger() + b.toInteger()
            //assert CalcRs.result.text().toInteger() == result
        }

        where: //Параметры теста
        [a, b] << sql.rows("Select a, b from confAB")

    }
}
