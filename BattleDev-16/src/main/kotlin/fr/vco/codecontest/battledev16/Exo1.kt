package fr.vco.codecontest.battledev16

object Exo1 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val lines = input.toList()
        val result = lines.subList(1, lines.size )
                .filter {
                    it.substring(it.length - 5, it.length).toIntOrNull() != null
                }
        println(result.size)

    }
}