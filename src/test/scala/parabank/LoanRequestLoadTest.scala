package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import parabank.Data._

class LoanRequestLoadTest extends Simulation {

  val feeder = csv("data/loanRequests.csv").circular
  
    // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition 
  val scn = scenario("Solicitud de préstamo bajo carga")
    .feed(feeder)
    .exec(http("Request Loan - ${fromAccountId}")
        .post("/requestloan")
        .body(
          StringBody(
            """
              {
                "amount": ${amount},
                "downPayment": ${downPayment},
                "fromAccountId": ${fromAccountId}
              }
            """
          )
        ).asJson
        .check(status.in(200, 201))
    ).pause(1, 3)

  // 3 Load Scenario
  setUp(
    scn.inject(
      nothingFor(5.seconds),
      rampConcurrentUsers(0) to (150) during (30.seconds), // subida progresiva
      constantConcurrentUsers(150) during (3.minutes),     // carga estable
      rampConcurrentUsers(150) to (0) during (30.seconds)  // bajada
    )
  ).protocols(httpConf)
    .assertions(
      global.responseTime.mean.lte(5000),        // Tiempo promedio ≤ 5 s
      global.successfulRequests.percent.gte(98)  // Éxito ≥ 98 %
    )
}
