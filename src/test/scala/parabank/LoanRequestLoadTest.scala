package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoanRequestLoadTest extends Simulation {

  // Configuración HTTP base
  val httpConf = http
    .baseUrl("https://parabank.parasoft.com/parabank")
    .acceptHeader("application/json, text/html, */*")
    .userAgentHeader("Gatling Load Test")
    .contentTypeHeader("application/x-www-form-urlencoded")

  // Feeder CSV
  val feeder = csv("data/loanRequests.csv").circular

  // Paso 1: Login
  val login = exec(
    http("Login")
      .post("/login.htm")
      .formParam("username", "john")   // usuario válido de demo
      .formParam("password", "demo")
      .check(status.is(200))
  )

  // Paso 2: Solicitud de préstamo (ya autenticado)
  val requestLoan = feed(feeder)
    .exec(
      http("Request Loan")
        .post("/services_proxy/bank/requestLoan")
        .queryParam("customerId", "${customerId}")
        .queryParam("fromAccountId", "${fromAccountId}")
        .queryParam("amount", "${amount}")
        .queryParam("downPayment", "${downPayment}")
        .check(status.is(200))
    )

  // Escenario principal
  val scn = scenario("Solicitud de préstamo bajo carga")
    .exec(login)
    .pause(1)
    .exec(requestLoan)

  // Inyección de usuarios
  setUp(
    scn.inject(rampUsers(50).during(10.seconds))
  ).protocols(httpConf)
}


