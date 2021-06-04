package fr.vco.codecontest.battledev17

object Exo3 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine)
        val lines = input.toList()

        val possibleIndex = MutableList(10){true}
        var index = -1
        var nbLine = 0
        lines.forEachIndexed { i, line ->
            if (nbLine == 4 && line[index] != '.') {
                println("BOOM ${index + 1}")
                return
            }
            line.forEachIndexed{c,it -> if( it == '#') possibleIndex[c] = false}
            if (line.count { it == '.' } == 1) {
                val newIndex = line.indexOf('.')
                if(newIndex == index && possibleIndex[newIndex]) {
                    nbLine++
                } else {
                    index = newIndex
                    nbLine = 1
                }
            } else {
                nbLine = 0
            }
        }
        if (nbLine == 4) println("BOOM ${index + 1}")
        else println("NOPE")
    }
}

