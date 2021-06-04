package fr.vco.codecontest.battledev17

object Exo2 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val lines = input.toList().drop(1)
        println(lines.groupBy{it}.filter{it.value.size == 2}.keys.first())
    }
}