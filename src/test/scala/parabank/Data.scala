package parabank

import scala.util.Random

object Data{
    // Base URL for Parabank API
    val url = "https://parabank.parasoft.com/parabank/services/bank"
    
    // Test users credentials
    val testUsers = Array(
        ("john", "demo"),
        ("jessica", "demo"),
        ("admin", "admin"),
        ("user", "password"),
        ("test", "test")
    )
    
    // Payment services data
    val paymentServices = Array(
        ("Electric Company", "12345"),
        ("Water Company", "67890"),
        ("Gas Company", "54321"),
        ("Internet Provider", "98765"),
        ("Phone Company", "11111")
    )
    
    // Account numbers for testing
    val accountNumbers = Array("12345", "67890", "54321", "98765", "11111")
    
    // Amounts for payments (in cents to avoid decimals)
    val paymentAmounts = Array(5000, 7500, 10000, 12500, 15000) // $50-$150
    
    // Helper functions
    def getRandomUser: (String, String) = testUsers(Random.nextInt(testUsers.length))
    def getRandomService: (String, String) = paymentServices(Random.nextInt(paymentServices.length))
    def getRandomAccount: String = accountNumbers(Random.nextInt(accountNumbers.length))
    def getRandomAmount: Int = paymentAmounts(Random.nextInt(paymentAmounts.length))
}