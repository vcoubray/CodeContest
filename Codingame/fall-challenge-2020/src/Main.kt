import java.util.*
import kotlin.collections.HashMap
import kotlin.math.min

fun log(message: String) = System.err.println(message)

const val BREW = "BREW"
const val CAST = "CAST"
const val LEARN = "LEARN"
const val REST = "REST"
const val OPPONENT_CAST = "OPPONENT_CAST"

val REST_ACTION = Action(-1, "REST", Ingredients(0, 0, 0, 0), 0, -1, 0, true, false)

data class Ingredients(
    private val ing0: Int,
    private val ing1: Int,
    private val ing2: Int,
    private val ing3: Int
) {
    operator fun plus(compos: Ingredients) = Ingredients(
        ing0 + compos.ing0,
        ing1 + compos.ing1,
        ing2 + compos.ing2,
        ing3 + compos.ing3
    )

    fun isValid() = when {
        ing0 < 0 -> false
        ing1 < 0 -> false
        ing2 < 0 -> false
        ing3 < 0 -> false
        sum() > 10 -> false
        else -> true
    }

    fun sum() = ing0 + ing1 + ing2 + ing3
    fun canAfford(taxe: Int) = this.ing0 >= taxe
    fun cost(): Int {
        var cost = 0
        cost += if (ing0 < 0) ing0 else 0
        cost += if (ing1 < 0) ing1 else 0
        cost += if (ing2 < 0) ing2 else 0
        cost += if (ing3 < 0) ing3 else 0
        return cost
    }

    fun gain(): Int {
        var cost = 0
        cost += if (ing0 > 0) ing0 else 0
        cost += if (ing1 > 0) ing1 else 0
        cost += if (ing2 > 0) ing2 else 0
        cost += if (ing3 > 0) ing3 else 0
        return cost
    }
}


data class Action(
    val id: Int,
    val type: String,
    val deltas: Ingredients,
    val price: Int,
    val tomeIndex: Int,
    val taxCount: Int,
    val castable: Boolean,
    val repeatable: Boolean
) {
    constructor(input: Scanner) : this(
        id = input.nextInt(),
        type = input.next(),
        deltas = Ingredients(input.nextInt(), input.nextInt(), input.nextInt(), input.nextInt()),
        price = input.nextInt(),
        tomeIndex = input.nextInt(),
        taxCount = input.nextInt(),
        castable = input.nextInt() != 0,
        repeatable = input.nextInt() != 0
    )

    fun exec(times: Int = 1) = when (type) {
        BREW -> "BREW $id"
        CAST -> "CAST $id $times"
        LEARN -> "LEARN $id"
        REST -> "REST"
        else -> "WAIT"
    }

}

object Game {
    var actions = listOf<Action>()
    var myInventory = Inventory(Ingredients(0, 0, 0, 0), 0)
    var oppInventory = Inventory(Ingredients(0, 0, 0, 0), 0)
    var hasChange = false

    var brews = listOf<Action>()
    var casts = listOf<Action>()
    var oppCasts = listOf<Action>()
    var learns = listOf<Action>()


    fun update(input: Scanner) {
        val actionCount = input.nextInt() // the number of spells and recipes in play
        val updatedAction = List(actionCount) { Action(input) }
        val updatedInventory = Inventory(input)
        val updatedOppInventory = Inventory(input)
        val updatedCasts = updatedAction.filter { it.type == "CAST" }
        val updatedOppCasts = updatedAction.filter { it.type == "OPPONENT_CAST" }

        hasChange = when {
            myInventory.score != updatedInventory.score -> true
            oppInventory.score != updatedOppInventory.score -> true
            casts.size != updatedCasts.size -> true
            oppCasts.size != updatedOppCasts.size -> true
            else -> false
        }

        this.actions = updatedAction
        this.myInventory = updatedInventory
        this.oppInventory = updatedOppInventory
        this.casts = updatedCasts
        this.oppCasts = updatedOppCasts
        this.learns = actions.filter { it.type == "LEARN" }
        this.brews = actions.filter { it.type == "BREW" }

    }

}

data class Actions(
    val brew: List<Action>,
    val cast: List<Action>,
    val oppCast: List<Action>,
    val learn: List<Action>
)

fun List<Action>.toActions() = Actions(
    this.filter { it.type == "BREW" },
    this.filter { it.type == "CAST" },
    this.filter { it.type == "OPPONENT_CAST" },
    this.filter { it.type == "LEARN" }
)


data class Inventory(
    val inv: Ingredients,
    val score: Int
) {
    constructor(input: Scanner) : this(
        inv = Ingredients(input.nextInt(), input.nextInt(), input.nextInt(), input.nextInt()),
        score = input.nextInt()
    )

}

data class StateTransition(
    val action: Action,
    val newInv: Ingredients,
    val times: Int = 1
) {
    fun exec() = when (action.type) {
        BREW -> "BREW ${action.id}"
        CAST -> "CAST ${action.id} $times"
        LEARN -> "LEARN ${action.id}"
        REST -> "REST"
        else -> "WAIT"
    }
}

//data class StateHashcode(
//    val inv: Ingredients,
//    val score: Int,
//    val casts: Map<Int, Boolean>
//    )

class State(
    val inv: Ingredients,
    val casts: Map<Int, Boolean>,
    val brews: Map<Int, Boolean>,
    val learns: Map<Int, Boolean>,
    val score: Int,
    val depth: Int = 0,
    val transition: StateTransition? = null,
    val parent: State? = null,
    var children : List<State>? = null
) {
    val hash = hashCode()

    fun transform(transition: StateTransition) = when (transition.action.type) {
        CAST -> cast(transition)
        BREW -> brew(transition)
        LEARN -> learn(transition)
        REST -> rest()
        else -> rest() // SHOULD NOT HAPPEN !
    }

    private fun rest() = State(inv, casts.map { it.key to true }.toMap(), brews, learns, score, depth + 1, StateTransition(REST_ACTION, inv),this)
    private fun cast(spell: StateTransition): State {
        val newCasts = casts.toMutableMap()
        newCasts[spell.action.id] = false
        return State(spell.newInv, newCasts, brews, learns, this.score, depth + 1, spell,this)
    }

    private fun learn(learn: StateTransition): State {
        val newLearns = learns.toMutableMap()
        newLearns.remove(learn.action.id)
        val newCasts = casts.toMutableMap()
        newCasts[learn.action.id] = true
        return State(learn.newInv, newCasts , brews,  newLearns, score, depth + 1, learn,this)
    }

    private fun brew(brew: StateTransition): State {
        val newBrews = brews.toMutableMap()
        newBrews.remove(brew.action.id)
        return State(brew.newInv, casts, newBrews, learns, this.score + brew.action.price, depth + 1, brew,this)
    }


    fun children(actions: List<Action>): List<State> {
//        if (children == null ) {
//            children = possibleTransitions(actions).map(::transform)
//        }
//        return children!!

        return possibleTransitions(actions).map(::transform)
    }
    private fun possibleTransitions(actions: List<Action>): List<StateTransition> {
        val transitions = mutableListOf<StateTransition>()
        actions.forEach {
            when {
                casts[it.id] == true -> {
                    var newInv = inv + it.deltas
                    if (newInv.isValid()) {
                        transitions.add(StateTransition(it, newInv))
                    }

                    if (it.repeatable) {
                        var times = 2
                        newInv += it.deltas
                        while (newInv.isValid()) {
                            transitions.add(StateTransition(it, newInv, times))
                            newInv += it.deltas
                            times++
                        }
                    }
                }
                learns[it.id] == true -> {
                    if (inv.canAfford(it.tomeIndex)) {
                        val gain = min(10 - inv.sum(), it.taxCount - it.tomeIndex)
                        val newInv = inv + Ingredients(gain, 0, 0, 0)
                        transitions.add(StateTransition(it, newInv))
                    }
                }
                it.type == BREW || it.type == REST -> {
                    val newInv = inv + it.deltas
                    if (newInv.isValid()) transitions.add(StateTransition(it, newInv))
                }
            }

        }
        return transitions
    }

    override fun hashCode(): Int {
        var result = inv.hashCode()
        result = 31 * result + casts.hashCode()
        result = 53 * result + score
        return result
    }

    override fun equals(other: Any?) = when {
        this === other -> true
        other is State -> (inv == other.inv) && (casts == other.casts) && (score == other.score)
        else -> false
    }

}


object BFS {
    val availableActions: MutableList<Action> = mutableListOf()
    val visited: HashMap<Int, Boolean> = HashMap(200000)
    val toVisit: LinkedList<State> = LinkedList()
    val possibleBrews: MutableList<State> = mutableListOf()

    fun reset() {
        val start = System.currentTimeMillis()
        log("start reset BFS")
        toVisit.clear()
        visited.clear()
        possibleBrews.clear()
        log("BFS reset in ${System.currentTimeMillis() - start}ms")
    }


    fun explore(root: State, actions: List<Action>, timeout: Int = 20): BFSSummary {
        val start = System.currentTimeMillis()
        val summary = BFSSummary()
        summary.timeout = timeout


        toVisit.add(root)
        visited[root.hash] = true

        var depthSummary = DepthSummary(root.depth,System.currentTimeMillis(),0,1)
        summary.depths.add(depthSummary)
        val end = start + timeout

        while (toVisit.isNotEmpty() && System.currentTimeMillis() < end ) {
            val current = toVisit.removeFirst()

            current.children(actions)
                .forEach {
                    if (System.currentTimeMillis() >= end) return@forEach

                    if (visited[it.hash] == null) {
                        if (it.transition?.action?.type == BREW) possibleBrews.add(it)
                        toVisit.add(it)
                        visited[it.hash] = true

                        summary.nodes++
                        if(it.depth == depthSummary.depth) {
                            depthSummary.nodes++
                        }else {
                            depthSummary.endTime = System.currentTimeMillis()
                            depthSummary = DepthSummary(it.depth,System.currentTimeMillis(),0,1)
                            summary.depths.add(depthSummary)
                        }

                    }
                }

        }
        log("End while: ${System.currentTimeMillis() - start}ms")
        log("possible brews : ${possibleBrews.size}")
        depthSummary.endTime = System.currentTimeMillis()
        summary.executionTime = System.currentTimeMillis() - start
        return summary

    }

    fun findRootAction(state: State, depth: Int = 1): StateTransition? {
        var current = state
        while (current.depth > depth) {
            current = current.parent!!
        }
        return current.transition
    }

    fun findBestBrew(): StateTransition {
        return possibleBrews.maxWith(compareBy({ it.score.toDouble() / it.depth }, { it.score }))
            ?.let { findRootAction(it) }
            ?: StateTransition(REST_ACTION, REST_ACTION.deltas)
    }

}

class BFSSummary(
    var nodes : Int = 0,
    var timeout: Int = 0,
    var executionTime : Long =0 ,
    val depths: MutableList<DepthSummary> = mutableListOf()
) {

    override fun toString(): String {
        val message = "BFS process $nodes in ${executionTime}ms for a timeout of ${timeout}ms"
        return message + "\n" + depths.joinToString("\n")
    }

}

class DepthSummary(
    val depth : Int,
    val startTime: Long,
    var endTime: Long = 0,
    var nodes: Int = 0
) {

    override fun toString(): String {
        return "Depth [$depth] : $nodes nodes explored in ${endTime-startTime}ms"
    }
}


fun main() {
    val input = Scanner(System.`in`)

    repeat(100) { turn ->

        val startReading = System.currentTimeMillis()
        Game.update(input)
        log("read input in ${System.currentTimeMillis() - startReading}ms")

        val start = System.currentTimeMillis()

        BFS.reset()

        val availableActions = Game.casts + Game.brews /*+ Game.learns*/ + REST_ACTION

        val bfsTimeout = if (turn == 0) 800 else 15

        val casts = Game.casts.map { it.id to it.castable }.toMap()
        val brews = Game.brews.map { it.id to true }.toMap().toMutableMap()
        val learns = Game.learns.map { it.id to true }.toMap().toMutableMap()

        val root = State(Game.myInventory.inv, casts, brews, learns, Game.myInventory.score)

        log("init in ${System.currentTimeMillis() - start}ms")

        if (turn < 6) {

            val currentIngs = Game.casts.map { it.deltas }.reduce { a, b -> a + b }
            val possibleLearn = Game.learns
                .asSequence()
                .filter { it.tomeIndex <= 1 }
                .filter { Game.myInventory.inv.canAfford(it.tomeIndex) }
                .filter { Game.oppInventory.inv.canAfford(it.tomeIndex) }
                .map { it to currentIngs + it.deltas }
                .sortedWith(compareBy({ -it.second.cost() }, { -it.second.gain() }))



            val learn = possibleLearn.firstOrNull()!!.first

            val summary = BFS.explore(root, availableActions, bfsTimeout)
           // log("$summary")
            println(learn.exec())

        } else {

            val summary = BFS.explore(root, availableActions, bfsTimeout)
            //log("$summary")
            val action = BFS.findBestBrew()
            log("End turn in ${System.currentTimeMillis() - start}ms")
            println(action.exec())
        }

    }
}