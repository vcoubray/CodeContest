package fr.vco.contest.euroInfo2020


fun main(args : Array<String>) {
    val input = generateSequence(::readLine)
    val lines = input.toList()
    lines.subList(1,lines.size).groupBy{it}.maxBy { it.value.size  }?.let{println(it.key)}
}