package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import parabank.Data._

class LoginTest extends Simulation{

  // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition
  val scn = scenario("Login").
    exec(http("login")
      .get(s"/login/$username/$password")
       //Recibir información de la cuenta
      .check(status.is(200))
    )

  // 3 Load Scenario
  setUp(
    scn.inject(
      rampConcurrentUsers(100).during(10), // carga normal
      //constantConcurrentUsers(100).during(30), // mantener carga para ver luego cómo se comporta durante ese tiempo
      //rampConcurrentUsers(100).to(200).during(10), // escalar a carga pico
      //constantConcurrentUsers(200).during(30) // mantener la carga pico para ver su comportamiento también
    )
  ).protocols(httpConf);

}



