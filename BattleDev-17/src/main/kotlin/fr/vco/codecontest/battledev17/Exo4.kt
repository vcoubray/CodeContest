package fr.vco.codecontest.battledev17

object Exo4 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val debris = input.toList().last()

        var result =0
        val size = debris.length/2
        val valid = debris.groupBy{it}.map{it.key to it.value.size/2}.toMap()
        val firstOrbit = debris.substring(0, size).groupBy{it}.map{it.key to it.value.size}.toMap()
        val current = valid.map{ it.key to firstOrbit.getOrDefault(it.key,0) }.toMap().toMutableMap()

        System.err.println(valid)
        System.err.println(current)
        for(i in debris.indices) {
            if(current == valid) result++
            val charToAdd = debris[(i+size)%debris.length]
            val charToRemove =debris[i%debris.length]
            current[charToAdd] = current[charToAdd]!! +1
            current[charToRemove] = current[charToRemove]!! -1
        }
        println(result)
    }
}