package fr.vco.codecontest.battledev16

object Exo4 {

    // Copy only the main method in the Isograd platform
    fun main() {

        val input = generateSequence(::readLine).toList()
        val (n, m) = input[0].split(" ").map { it.toInt() }
        val key = input[1].split(" ").map { it.toInt() }
        val messages = input.subList(2, input.size)

        val cache = mutableListOf<Int>()
        key.forEachIndexed { i, k ->
            if (i == 0) cache.add(k)
            else cache.add(cache[i - 1] xor k)
        }

        val result = MutableList(256) { 0 }
        messages.forEach { m ->
            val (l, r) = m.split(" ").map { it.toInt() }
            val i = if (l == 0) cache[r]
            else cache[l - 1] xor cache[r]
            result[i]++
        }
        println(result.joinToString(separator = " "))
    }

}