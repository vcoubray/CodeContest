package fr.vco.codecontest.battledev16

object Exo2 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine).toList()
        val lines = input.subList(1,input.size)

        val (night, day) = lines.map{it.substring(0,2).toInt()}.partition{it >= 20 || it <= 7 }
        val sus = if (night.size > day.size) "SUSPICIOUS" else "OK"
        println(sus)


    }
}