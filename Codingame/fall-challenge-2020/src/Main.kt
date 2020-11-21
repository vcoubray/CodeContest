
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

fun log(message: String) = System.err.println(message)

const val BREW = "BREW"
const val CAST = "CAST"
const val LEARN = "LEARN"
const val REST = "REST"
const val OPPONENT_CAST = "OPPONENT_CAST"

//val REST_ACTION = Action(0, "REST", Compos(List(4) { 0 }), 0, -1, 0, true, false)
val REST_ACTION = Action(0, "REST", Compos(0, 0, 0, 0), 0, -1, 0, true, false)


//data class Compos(
//    private val deltas: List<Int>
//) {
//    operator fun plus(compos: Compos): Compos {
//        return this.deltas.mapIndexed { i, delta -> delta + compos.deltas[i] }.let { Compos(it) }
//    }
//
////    fun dist(target: Compos): Int {
////        val result = plus(target)
////        return result.deltas.filter { it < 0 }.sum().unaryMinus()
////    }
//
//    fun isValid() = when {
//        deltas.any { it < 0 } -> false
//        deltas.sum() > 10 -> false
//        else -> true
//    }
//
//}


data class Compos(
    private val ing0: Int,
    private val ing1: Int,
    private val ing2: Int,
    private val ing3: Int
) {
    operator fun plus(compos: Compos) = Compos(
        ing0 + compos.ing0,
        ing1 + compos.ing1,
        ing2 + compos.ing2,
        ing3 + compos.ing3
    )

//    operator fun times(factor: Int) = Compos(
//        ing0 * factor,
//        ing1 * factor,
//        ing2 * factor,
//        ing3 * factor
//    )

    fun isValid() = when {
        ing0 < 0 -> false
        ing1 < 0 -> false
        ing2 < 0 -> false
        ing3 < 0 -> false
        sum() > 10 -> false
        else -> true
    }

    fun sum() = ing0 + ing1 + ing2 + ing3

    fun hasEnoughIng0(ing : Int) = this.ing0 >= ing

    fun canLearn(action: Action) = action.type == LEARN && action.tomeIndex <= ing0
}


data class Action(
    val id: Int,
    val type: String,
    val deltas: Compos,
    val price: Int,
    val tomeIndex: Int,
    val taxCount: Int,
    val castable: Boolean,
    val repeatable: Boolean
) {
    constructor(input: Scanner) : this(
        id = input.nextInt(),
        type = input.next(),
        //deltas = Compos(List<Int>(4) { input.nextInt() }),
        deltas = Compos(input.nextInt(), input.nextInt(), input.nextInt(), input.nextInt()),
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

class Game() {
    var actions = mutableListOf<Action>()
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
    val inv: Compos,
    val score: Int
) {
    constructor(input: Scanner) : this(
        //inv = Compos(List<Int>(4) { input.nextInt() }),
        inv = Compos(input.nextInt(), input.nextInt(), input.nextInt(), input.nextInt()),
        score = input.nextInt()
    )

}

data class StateTransition (
    val action : Action,
    val newInv : Compos,
    val times : Int = 1
){
    fun exec() = when (action.type) {
        BREW -> "BREW ${action.id}"
        CAST -> "CAST ${action.id} $times"
        LEARN -> "LEARN ${action.id}"
        REST -> "REST"
        else -> "WAIT"
    }
}


data class State(
    val inv: Compos,
    val casts: Map<Int, Boolean>,
    val score: Int,
    val depth: Int = 0,
    val transition: StateTransition? = null
    //val action: Action? = null
) {
    fun isValidAction(action: Action) = when {
        action.type == CAST && !(casts[action.id] ?: false) -> false
        else -> inv.plus(action.deltas).isValid()
    }

    fun transform(transition: StateTransition) = when (transition.action.type) {
        CAST -> cast(transition)
        BREW -> brew(transition)
        LEARN -> learn(transition)
        REST -> rest()
        else -> wait() // SHOULD NOT HAPPEND
    }

    private fun rest() = State(inv, casts.map { it.key to true }.toMap(), score, depth + 1, StateTransition(REST_ACTION,inv))
    private fun cast(spell: StateTransition): State {
        val newCasts = casts.toMutableMap()
        newCasts[spell.action.id] = false
        return State(spell.newInv, newCasts, this.score, depth + 1, spell)
    }

    private fun learn(learn: StateTransition) = State(learn.newInv, casts, score, depth + 1,learn)
    private fun brew(brew: StateTransition) = State(brew.newInv, casts, this.score + brew.action.price, depth + 1, brew)
    private fun wait() = this.copy()

    //fun children(actions: List<Action>): List<State> = actions.filter(::isValidAction).map(::transform)

    fun children(actions: List<Action>): List<State> =possibleTransitions(actions).map(::transform)

    fun possibleTransitions(actions:List<Action>) :List<StateTransition> {
        val transitions = mutableListOf<StateTransition>()
        actions.forEach{

            if (it.type == CAST && casts[it.id] == true)   {
                var newInv = inv + it.deltas
                if( newInv.isValid()) {
                    transitions.add(StateTransition(it,newInv))
                }

                if (it.repeatable) {
                    var times = 2
                    newInv += it.deltas
                    while ( newInv.isValid() ){
                        transitions.add(StateTransition(it, newInv, times))
                        newInv += it.deltas
                        times++
                    }
                }
            }
            if( it.type == LEARN) {
                if(inv.hasEnoughIng0( it.tomeIndex)) {
                    val gain = min(10-inv.sum() , it.taxCount - it.tomeIndex  )
                    val newInv = inv + Compos(gain,0,0,0)
                    transitions.add(StateTransition(it, newInv))
                }

            }
            if (it.type != CAST && it.type != LEARN){
                val newInv = inv + it.deltas
                if (newInv.isValid()) transitions.add(StateTransition(it, newInv))
            }

        }
        return transitions
    }

    override fun hashCode(): Int {
        var result = inv.hashCode()
        result = 31 * result + casts.hashCode()
        result = 31 * result + score
        return result
    }

//
//    fun findRootAction(depth: Int = 1): Action? {
//        var state = this
//        while (state.depth > depth) {
//            state = state.parent!!
//        }
//        return state.action
//    }

    override fun equals(other: Any?) =
        when {
            this === other -> true
            other is State -> (inv == other.inv) && (casts == other.casts) && (score == other.score)
            else -> false
        }


}


object BFSOptions {
    val availableActions: MutableList<Action> = mutableListOf()
    val visited: HashMap<State, State?> = HashMap(50000)
    val toVisit: LinkedList<State> = LinkedList()
    val possibleBrews: MutableList<State> = mutableListOf()


//    fun findRootAction(state: State, depth: Int = 1): Action? {
//        var current = state
//        while (visited[current]!=null) {
//            current = visited[current]!!
//        }
//        return current.action
//    }
//
//    fun findBestBrew(): Action {
//        return possibleBrews.maxBy { it.score / it.depth }
//            ?.let(this::findRootAction)
//            ?:REST_ACTION
//    }

}

fun BFS(root: State, actions: List<Action>, timeout: Int = 20): StateTransition {

    val depthMap = MutableList(100) { 0 }
    // val toVisit = ArrayDeque<State>()
    val toVisit = LinkedList<State>()
    val visited = HashMap<State, State?>(5000)
    val result = mutableListOf<State>()

    toVisit.add(root)
    visited[root] = null
    val start = System.currentTimeMillis()
    val end = start + timeout;
    var i = 0
    log("start BFS")
    while (toVisit.isNotEmpty() && System.currentTimeMillis() < end) {
        val current = toVisit.removeFirst()
        depthMap[current.depth]++
        current.children(actions)
            .forEach {
                if (visited[it] == null) {
                    if (it.transition?.action?.type == BREW) result.add(it)
                    //else {
                    toVisit.add(it)
                    //}
                    visited[it] = current
                }
            }
        i++
    }
    log("process $i elements in ${end - start} ms")
    log("$depthMap")



    return findBestBrew(result, visited)
}


fun findRootAction(state: State, visited: HashMap<State, State?>, depth: Int = 1): StateTransition? {
    var current = state
    while (current.depth > depth) {
        current = visited[current]!!
    }
    return current.transition
}

fun findBestBrew(possibleBrews: List<State>, visited: HashMap<State, State?>): StateTransition {
    return possibleBrews.maxWith (compareBy({ it.score.toDouble() / it.depth},{it.score}) )
        ?.let { findRootAction(it, visited) }
        ?: StateTransition(REST_ACTION,REST_ACTION.deltas)
}

fun learnScore(learn: Action, spells: List<Action>) {
    val cost = learn.taxCount - learn.tomeIndex
    val actualDiff = spells.map { it.deltas }.reduce { sum, it -> sum + it }
    val learnDiff = actualDiff + learn.deltas

    //actualDiff.

}

fun main() {
    val input = Scanner(System.`in`)

    var maxIter = 0
    repeat(100) { turn ->
        // while (true) {
        val start = System.currentTimeMillis()
        val actionCount = input.nextInt() // the number of spells and recipes in play


        val actions = List(actionCount) { Action(input) }.toActions()
        val availableActions = actions.cast + actions.brew + actions.learn + REST_ACTION

        val bfsTimeout = if (turn == 0) 800 else 17

        val myInventory = Inventory(input)
        val oppInventory = Inventory(input)
        log("read input in ${System.currentTimeMillis() - start}ms")
        val start2 = System.currentTimeMillis()

        val casts = actions.cast.map { it.id to it.castable }.toMap()
        val root = State(myInventory.inv, casts, myInventory.score)

        log("init in ${System.currentTimeMillis() - start2}ms")

        if (turn < 6) {

            val learn = actions.learn.sortedBy { it.tomeIndex }.firstOrNull()!!


            val results = BFS(root, availableActions, bfsTimeout)
            log("end BFS in ${System.currentTimeMillis() - start2}ms - max Iter = $maxIter")
            println(learn.exec())

        } else {

            val action = BFS(root, availableActions, bfsTimeout)
            log("end BFS in ${System.currentTimeMillis() - start2}ms - max Iter = $maxIter")
            //log("results count: ${results.size}")
//            val action = results.maxBy { it.score / it.depth }?.findRootAction()
//                ?: root.children(actions.cast).firstOrNull()?.action
//                ?: REST_ACTION
            println(action.exec())
        }
        log("end in ${System.currentTimeMillis() - start2}ms")
    }
}