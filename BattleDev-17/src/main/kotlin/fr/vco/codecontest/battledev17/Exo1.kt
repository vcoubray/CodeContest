package fr.vco.codecontest.battledev17

object Exo1 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val lines = input.toList()
        val (D,L) = lines.map{it.toInt()}
        println("${D+L*5}")
    }
}