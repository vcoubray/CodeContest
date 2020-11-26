package fr.vco.codecontest.battledev16

object Exo4 {

    // Copy only the main method in the Isograd platform
    fun main() {

        val input = generateSequence(::readLine).toList()
        val (n,m) = input[0].split(" ").map{it.toInt()}
        val key = input[1].split(" ").map{it.toInt()}
        val messages = input.subList(2,input.size)

        val result = MutableList(256){0}



        messages.forEach{m ->
            val (l,r) = m.split(" ").map{it.toInt()}
            var i = key[l]
            for (j in l+1..r){
                i= i.xor(key[j])
            }
            //val i = key.subList(l,r+1).reduce{a,b->a.xor(b)}
        }
        println(result.joinToString (separator=" ") )
    }

}