package fr.vco.codecontest.battledev16

import java.util.LinkedList

object Exo3 {

    // Copy only the main method in the Isograd platform
    fun main() {
        val input = generateSequence(::readLine).toList()
        val lines = input.subList(1,input.size)

        val agents = mutableMapOf<Int,Agent>()
        lines.forEach{
            val (childId, parentId) =it.split(" ").map{it.toInt()}

            val parent=agents[parentId]?:Agent(parentId,null)
            val child=agents[childId]?:Agent(parentId,null)
            agents[parentId] = parent
            agents[childId] = child

            child.parent = parent
            parent.children.add(child)
        }

        val levels = MutableList(10){0}
        val root = agents[0]!!
        val toVisit = LinkedList<Agent>()
        toVisit.add(root)
        while(toVisit.isNotEmpty()){

            val current = toVisit.remove()
            levels[current.lvl]++
            current.children.forEach{
                it.lvl = current.lvl+1
                toVisit.add(it)
            }
        }

        println(levels.joinToString (separator =" " ))

    }

    class Agent(
            val id:Int,
            var parent:Agent?,
            val children: MutableList<Agent> = mutableListOf(),
            var lvl :Int = 0
    )

}