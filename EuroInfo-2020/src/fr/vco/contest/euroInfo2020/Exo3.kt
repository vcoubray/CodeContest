package fr.vco.contest.euroInfo2020

import java.util.LinkedList

fun main(args : Array<String>) {
    val input = generateSequence(::readLine)
    val lines = input.toList()
    val (server_count,link_count) = lines[0].split(" ").map{it.toInt()}

    val servers = List(server_count){i-> Server(i+1)}
    lines.subList(1,lines.size).forEach{ line ->
        val (a,b) = line.split(" ").map{it.toInt()}
        servers[a-1].children.add(servers[b-1])
    }

    servers.map{it.id to it.getTotalChildren()}.maxBy{it.second }?.let{println(it.first)}
}

class Server(val id : Int, val children: MutableList<Server> = mutableListOf()) {
    fun getTotalChildren() : Int{
        var visited = 1
        val toVisit = LinkedList<Server>()
        toVisit.add(this)
        while(toVisit.isNotEmpty()) {
            val current = toVisit.pop()
            visited += current.children.size
            current.children.forEach{toVisit.add(it)}
        }
        return visited
    }
}