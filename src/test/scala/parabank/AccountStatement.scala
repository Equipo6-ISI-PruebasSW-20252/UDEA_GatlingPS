package parabank 

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._

class AccountStatementTest extends Simulation{

  val accountFeeder = Data.accountNumbers.map(id => Map("account_id" -> id)).circular

  // 1 Http Conf 
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition 
  val scn = scenario("AccountStatement")
  .feed(accountFeeder)
  .exec(http("account_statement")
      .get("/accounts/${account_id}")
       //Recibir información de la cuenta
      .check(status.is(200))
    )

  // 3 Load Scenario  
 setUp(
  scn.inject(
    rampConcurrentUsers(0).to(100).during(10),
    constantConcurrentUsers(100).during(30),
    rampConcurrentUsers(100).to(200).during(10),
    constantConcurrentUsers(200).during(30)
  )
   
).protocols(httpConf)

  .assertions(
    // Criterio de aceptación de respuesta menor a 3s
    global.responseTime.mean.lte(3000),   // tiempo promedio ≤ 3s
    global.failedRequests.percent.lte(1), // ≤ 1% de errores
  )

}
