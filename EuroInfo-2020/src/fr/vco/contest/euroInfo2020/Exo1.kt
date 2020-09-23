package fr.vco.contest.euroInfo2020

import kotlin.text.*

fun main(args : Array<String>) {
    val input = generateSequence(::readLine)
    val lines = input.toList()
    lines.subList(1, lines.size).firstOrNull { it.substring(5, 8) != "555" }.let { println(it) }
}