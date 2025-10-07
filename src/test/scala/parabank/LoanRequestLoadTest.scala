package parabank

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LoanRequestLoadTest extends Simulation {

  val feeder = csv("data/feeder.csv").circular
  
    // 1 Http Conf
  val httpConf = http.baseUrl(url)
    .acceptHeader("application/json")
    //Verificar de forma general para todas las solicitudes
    .check(status.is(200))

  // 2 Scenario Definition 
  val scn = scenario("Solicitud de préstamo bajo carga")
 
  

  
  

  
}
