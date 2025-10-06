package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._

class TrabsfersTest extends Simulation{


  val feeder = csv("transfers.csv").circular

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition
  val scn = scenario("Transferencias simultáneas")
    .feed(feeder)
    .exec(http("Transferencias simultáneas")
      .post(s"/transfer")
      .queryParam("fromAccountId", origen)
      .queryParam("toAccountId", destino)  
      .queryParam("amount", cantidad)
      .check(status.is(200))
    )

  // 3 Load Scenario
  setUp(
    scn.inject(
      rampUsers(150).during(10),
      constantUsersPerSec(100).during(30),
      rampUsers(200).during(10), // escalar a carga pico
      constantUsersPerSec(200).during(30) // mantener la carga pico para ver su comportamiento también
    )
  ).protocols(httpConf);

}



