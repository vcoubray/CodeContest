package fr.vco.codecontest.battledev17

import kotlin.math.min

object Exo5 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val lines = input.toList().map { l -> l.split(" ").map { it.toInt() } }
        val (n, a, c) = lines.first()
        val asteroid = lines.last()

        System.err.println(" $n, $a, $c")
        System.err.println(asteroid)

        val shields = asteroid.mapIndexed { i, _ -> i to asteroid.subList(i, min(asteroid.size, i + a)).sum() }
        //System.err.println(shields)

    }
}