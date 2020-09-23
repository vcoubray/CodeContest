package fr.vco.contest.euroInfo2020

import java.util.LinkedList

fun main(args: Array<String>) {
    val input = generateSequence(::readLine)
    val lines = input.toList()
    val (h, _) = lines[0].split(" ").map { it.toInt() }

    val stage = List(h) { i -> lines[i + 1] }

    stage.forEach(System.err::println)

    val results = mutableListOf<Pos>()
    val visited = mutableSetOf<Pos>()
    stage.forEachIndexed { y, line ->
        line.forEachIndexed { x, _ ->
            val pos = Pos(x, y)
            if (!visited.contains(pos)) {
                val shape = stage.getShape(pos)
                visited.addAll(shape)
                if (shape.isRect()) {
                    results.add(shape.getCorner())
                }
            }
        }
    }

    println(results.size)
    results.forEach { println("${it.y + 1} ${it.x + 1}") }

}

fun List<String>.getShape(pos: Pos): List<Pos> {
    val visited = mutableSetOf<Pos>()
    val toVisit = LinkedList<Pos>()
    val char = this[pos.y][pos.x]
    toVisit.add(pos)

    while (toVisit.isNotEmpty()) {
        val current = toVisit.pop()
        this.neighbours(current)
                .filter { getChar(it) == char }
                .filterNot { visited.contains(it) }
                .forEach { toVisit.add(it) }
        visited.add(current)
    }
    return visited.toList()
}

fun List<String>.getChar(pos: Pos) = this[pos.y][pos.x]

fun List<String>.neighbours(pos: Pos) =
        listOf(pos.copy(x = pos.x + 1),
                pos.copy(x = pos.x - 1),
                pos.copy(y = pos.y - 1),
                pos.copy(y = pos.y + 1)
        ).filter(this::isValid)

fun List<String>.isValid(pos: Pos) =
        when {
            pos.y < 0 -> false
            pos.y >= this.size -> false
            pos.x < 0 -> false
            pos.x >= this[0].length -> false
            else -> true
        }

fun List<Pos>.isRect(): Boolean {
    if (this.isEmpty()) return false
    val minX = this.minBy { it.x }!!.x
    val maxX = this.maxBy { it.x }!!.x
    val minY = this.minBy { it.y }!!.y
    val maxY = this.maxBy { it.y }!!.y

    val w = maxX - minX +1
    val h = maxY - minY +1

    return when {
        this.size != 8 -> false
        w == 4 && h == 2 -> true
        w == 2 && h == 4 -> true
        else -> false
    }
}

fun List<Pos>.getCorner(): Pos {
    val x = this.minBy { it.x }!!.x
    val y = this.minBy { it.y }!!.y
    return Pos(x, y)
}

data class Pos(val x: Int, val y: Int)
