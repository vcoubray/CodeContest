import java.util.*

fun log(message: String) = System.err.println(message)

const val BREW = "BREW"
const val CAST = "CAST"
const val LEARN = "LEARN"
const val REST = "REST"
const val OPPONENT_CAST = "OPPONENT_CAST"

data class Compos(
    private val deltas: List<Int>
) {
    operator fun plus(compos: Compos): Compos {
        return this.deltas.mapIndexed { i, delta -> delta + compos.deltas[i] }.let { Compos(it) }
    }

    /**
     * Indique le nombre d'ingrédient manquant pour atteindre la target
     */
    fun miss(target: Compos): Int {
        return plus(target).deltas.filter { it < 0 }.sum().unaryMinus()
        //return target.deltas.mapIndexed{i, it -> it - deltas[i] }.filter{it <0}.sum()
    }

    fun dist(target: Compos): Int {
        val result = plus(target)
        return result.deltas.filter { it < 0 }.sum().unaryMinus()
    }

    fun sum() = deltas.filter { it > 0 }.sum()

    fun isValid() = when {
        sum() > 10 -> false
        deltas.any { it < 0 } -> false
        else -> true
    }

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
        deltas = Compos(List<Int>(4) { input.nextInt() }),
        price = input.nextInt(),
        tomeIndex = input.nextInt(),
        taxCount = input.nextInt(),
        castable = input.nextInt() != 0,
        repeatable = input.nextInt() != 0
    )

    fun exec() = when (type) {
        BREW -> "BREW $id"
        CAST -> "CAST $id"
        LEARN -> "LEARN $id"
        REST -> "REST"
        else -> "WAIT"
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
    val inv: Compos,
    val score: Int
) {
    constructor(input: Scanner) : this(
        inv = Compos(List<Int>(4) { input.nextInt() }),
        score = input.nextInt()
    )

    override fun equals(other: Any?) =
        if (other is Inventory) {
            inv == other.inv
        } else false

    override fun hashCode(): Int {
        return super.hashCode()
    }

    fun transform(action: Action) = Inventory(inv + action.deltas, score)
    fun isValid() = inv.isValid()

}


class State(
    val inv: Inventory,
    val actions: List<Action>,
    //val depth : Int,
    val parentAction: Action? = null,
    var score: Int = 0,
    val parent: State? = null
) {

    lateinit var stateChildren: List<State>

     fun transform(action: Action): State {
        val newActions = if (action.type == CAST)
            this.actions.map { if (it.id == action.id) it.copy(castable = false) else it }
        else
            this.actions.map { it.copy(castable = true) }
        return State(inv.transform(action), newActions, action)
    }

    fun isValid() = inv.isValid()


    override fun equals(other: Any?): Boolean {
        return if (other is State) {
            inv == other.inv && actions == other.actions
        } else false
    }

    override fun hashCode(): Int {
        var result = inv.hashCode()
        result = 31 * result + actions.hashCode()
        return result
    }

    fun getChildren(): List<State> {
        if (!this::stateChildren.isInitialized) {
            stateChildren = actions
                .filter { it.castable }
                .map { transform(it) }
                .filter { it.isValid() }
        }
        return stateChildren
    }

    fun calcScore(target: Compos): Int {
        score =  inv.inv.dist(target)
        return score
    }

}

fun BFS(root: State, target: Compos, timeout: Int = 20) : Int {


    val toVisit = PriorityQueue<State>(compareBy{it.score})
    toVisit.add(root)
    val start = System.currentTimeMillis()
    val end = start + timeout;
    var i = 0
    log("start BFS $i")
    while (toVisit.isNotEmpty() && System.currentTimeMillis() < end) {
        val current = toVisit.remove()
//        current.score = current.calcScore(target)
        current.getChildren()
            .forEach {
                it.calcScore(target)
                toVisit.add(it)
            }
        i++
    }

    log("process $i elements in ${end - start} ms")
    return i

}

fun findAction(node: State): Action? {

    val actionsChain = mutableListOf<Action>()
    var current: State? = node
    while (current != null) {
        current.parentAction?.let {
            actionsChain.add(it)
        }
        current = current.parent
    }
    log("$actionsChain")
    return actionsChain.lastOrNull()
}


fun main() {
    val input = Scanner(System.`in`)

    var maxIter = 0
    repeat(100) { turn ->
        // while (true) {
        val start = System.currentTimeMillis()
        val actionCount = input.nextInt() // the number of spells and recipes in play

        val actions = List(actionCount) { Action(input) }.toActions()
        // val commands = actions.toCommands()


        val myInventory = Inventory(input)
        val oppInventory = Inventory(input)
        log("read input in ${System.currentTimeMillis() - start}ms")

        val start2 = System.currentTimeMillis()
        if(turn <10) {
            val learn = actions.learn.sortedBy { it.tomeIndex }.firstOrNull()!!
            println(learn.exec())
        } else {

            //val target = actions.brew.minBy { it.price - it.tomeIndex }!!
            val target = actions.brew.maxBy { it.price  }!!

            log("target : $target")

            val restAction = Action(0, "REST", Compos(List(4) { 0 }), 0, -1, 0, true, false)
            val root = State(myInventory, actions.cast + restAction)
            root.calcScore(target.deltas)

            log("init in ${System.currentTimeMillis() - start2}ms")
            val iter = BFS(root, target.deltas, 15)
            if (iter > maxIter) maxIter = iter
            log("end BFS in ${System.currentTimeMillis() - start2}ms - max Iter = $maxIter")

            val action = when {
                root.score == 0 -> target.exec()
                else -> root.stateChildren.sortedBy { it.score }.firstOrNull()?.parentAction?.exec()
                    ?: "WAIT"
            }
            println(action)
            log("end in ${System.currentTimeMillis() - start2}ms")
        }
//        measureTimeMillis {
//            val inventoryNode = getBestAction(myInventory, actions.cast, target)
//
//            val bestAction = findAction(inventoryNode)

        //        val currentDiff = myInventory.inv.miss(target.deltas)
        //
        //        val spellDiffs = actions.cast
        //            .filter { myInventory.transform(it).isValid() }
        //            .map { it to myInventory.inv.plus(it.deltas).miss(target.deltas) }

        //        spellDiffs.forEach { log("$it") }
        //        val bestSpell = spellDiffs
        //            .sortedWith(compareBy({ it.second }, { !it.first.castable }))
        //            .firstOrNull()!!.first


        //        log("$currentDiff")
//            val action = when {
//                myInventory.transform(target).isValid() -> target.exec()
//                else -> bestAction?.exec() ?: "REST"
//
//            }

        // println(action)

        //}.apply{log("$this")}
        //    }
    }
}