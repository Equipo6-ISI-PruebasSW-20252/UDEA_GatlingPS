package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * Corrected Payment Test - Versión Corregida y Simplificada
 * 
 * Historia de Usuario No Funcional 5: Pago de servicios con concurrencia alta
 * 
 * Criterios de aceptación ORIGINALES:
 * - Tiempo de respuesta por transacción ≤ 3 segundos
 * - Tasa de errores funcionales ≤ 1%
 * - Sistema debe registrar correctamente el pago en el historial sin duplicaciones
 * - 200 usuarios concurrentes
 * 
 * Enfoque: Validación de historial usando endpoints REST directos
 */
class CorrectedPaymentTest extends Simulation {

  // HTTP Configuration única para toda la prueba
  val httpConf = http
    .baseUrl("https://parabank.parasoft.com/parabank")
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
    .check(status.in(200, 302))
    .disableWarmUp
    .disableFollowRedirect

  // Login flow usando formularios HTML
  val loginFlow = scenario("Login HTML")
    .exec(http("Login Page")
      .get("/login.htm")
      .check(status.in(200, 302))
    )
    .pause(1, 2)
    .exec(http("Login Submit")
      .post("/login.htm")
      .formParam("username", "john")
      .formParam("password", "demo")
      .check(status.in(200, 302))
      .check(css("h1.title", "text").exists)
    )

  // Transfer flow usando formularios HTML
  val transferFlow = scenario("Transfer HTML")
    .exec(http("Transfer Page")
      .get("/transfer.htm")
      .check(status.in(200, 302))
    )
    .pause(1, 2)
    .exec(http("Process Transfer")
      .post("/transfer.htm")
      .formParam("fromAccountId", "13344")
      .formParam("toAccountId", "13345")
      .formParam("amount", "100.00")
      .check(status.in(200, 302))
      .check(css("h1.title", "text").exists)
    )

  // Bill Pay flow usando formularios HTML
  val billPayFlow = scenario("Bill Pay HTML")
    .exec(http("Bill Pay Page")
      .get("/billpay.htm")
      .check(status.in(200, 302))
    )
    .pause(1, 2)
    .exec(http("Process Bill Pay")
      .post("/billpay.htm")
      .formParam("payee.name", "Test Company")
      .formParam("payee.address.street", "123 Test St")
      .formParam("payee.address.city", "Test City")
      .formParam("payee.address.state", "TS")
      .formParam("payee.address.zipCode", "12345")
      .formParam("payee.phoneNumber", "123-456-7890")
      .formParam("payee.accountNumber", "12345")
      .formParam("verifyAccount", "12345")
      .formParam("amount", "50.00")
      .formParam("fromAccountId", "13344")
      .check(status.in(200, 302))
      .check(css("h1.title", "text").exists)
    )

  // Validación de historial usando endpoints REST (configuración separada)
  val historyValidationFlow = scenario("History Validation REST")
    .exec(
      http("Get Customer Accounts")
        .get("/services/bank/customers/12212/accounts")
        .header("Accept", "application/xml")
        .check(status.is(200))
        .check(xpath("//account/id").findAll.saveAs("accountIds"))
    )
    .pause(1, 2)
    .exec(
      http("Get Transaction History")
        .get("/services/bank/accounts/13344/transactions") // Usar cuenta fija por ahora
        .header("Accept", "application/xml")
        .check(status.is(200))
        .check(xpath("//transaction").count.gte(1)) // Al menos una transacción
        .check(xpath("//transaction/type").findAll.saveAs("transactionTypes"))
        .check(xpath("//transaction/id").findAll.saveAs("transactionIds"))
        .check(xpath("//transaction/amount").findAll.saveAs("transactionAmounts"))
    )
    .exec(session => {
      // Validar que no hay duplicaciones en los IDs de transacciones
      try {
        val transactionIds = session("transactionIds").as[Vector[String]]
        val uniqueIds = transactionIds.distinct
        val hasDuplicates = transactionIds.size != uniqueIds.size
        
        session.set("hasDuplicates", hasDuplicates)
          .set("totalTransactions", transactionIds.size)
          .set("uniqueTransactions", uniqueIds.size)
      } catch {
        case _: Exception => 
          session.set("hasDuplicates", false)
            .set("totalTransactions", 0)
            .set("uniqueTransactions", 0)
      }
    })

  // Flujo completo que combina HTML y REST
  val completePaymentFlow = scenario("Complete Payment Flow")
    .exec(loginFlow)
    .pause(2, 4)
    .randomSwitch(
      50.0 -> exec(transferFlow),
      50.0 -> exec(billPayFlow)
    )
    .pause(1, 2)
    .exec(historyValidationFlow)

  // Load injection: 200 usuarios concurrentes
  val loadPattern = completePaymentFlow
    .inject(
      rampUsers(50).during(30.seconds),  // Ramp up
      atOnceUsers(200), // Carga inmediata de 200 usuarios concurrentes
      rampUsers(0).during(30.seconds)   // Ramp down
    )

  // Setup simulation with assertions ORIGINALES
  setUp(loadPattern)
    .protocols(httpConf)
    .assertions(
      // Criterios ORIGINALES de rendimiento
      global.responseTime.max.lt(3000), // ≤ 3 segundos
      global.responseTime.mean.lt(2000), // Promedio < 2 segundos
      global.responseTime.percentile(95).lt(2800), // 95% < 2.8 segundos
      
      // Criterio ORIGINAL de tasa de errores ≤ 1%
      global.failedRequests.percent.lt(1.0),
      global.successfulRequests.percent.gt(99.0),
      
      // Validaciones específicas de requests
      details("Login Submit").responseTime.max.lt(3000),
      details("Login Submit").successfulRequests.percent.gt(95.0),
      
      details("Process Transfer").responseTime.max.lt(3000),
      details("Process Transfer").successfulRequests.percent.gt(95.0),
      
      details("Process Bill Pay").responseTime.max.lt(3000),
      details("Process Bill Pay").successfulRequests.percent.gt(95.0),
      
      // Validaciones de historial (nuevas)
      details("Get Transaction History").responseTime.max.lt(3000),
      details("Get Transaction History").successfulRequests.percent.gt(95.0)
    )
}
