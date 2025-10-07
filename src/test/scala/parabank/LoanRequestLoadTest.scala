package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoanRequestLoadTest extends Simulation {

  // 1️ Configuración HTTP
  val httpConf = http
    .baseUrl("https://parabank.parasoft.com/parabank/services_proxy/bank")
    .acceptHeader("application/json, text/plain, */*")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Load Test")

  // 2️ Datos de entrada
  val feeder = csv("data/loanRequests.csv").circular

  // 3️ Escenario principal
  val scn = scenario("Solicitud de préstamo bajo carga")
    .feed(feeder)
    .exec(
      http("Request Loan")
        .post("/requestLoan")
        .queryParam("customerId", "${customerId}")
        .queryParam("amount", "${amount}")
        .queryParam("downPayment", "${downPayment}")
        .queryParam("fromAccountId", "${fromAccountId}")
        .check(status.is(200))
    )

  // 4️ Inyección de usuarios (criterios de carga)
  setUp(
    scn.inject(
      rampUsers(150).during(10.seconds), // escalar hasta 150 usuarios
      constantUsersPerSec(150).during(30.seconds) // mantener carga 30s
    )
  ).protocols(httpConf)
}

