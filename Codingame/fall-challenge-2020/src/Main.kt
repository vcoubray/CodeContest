import java.util.*

fun log(message: String) = System.err.println(message)

data class Action(
    val id: Int,
    val type: String,
    val deltas: List<Int>,
    val price: Int,
    val tomeIndex: Int,
    val taxCount: Int,
    val castable: Boolean,
    val repeatable: Boolean
) {
    constructor(input: Scanner) : this(
        id = input.nextInt(),
        type = input.next(),
        deltas = List<Int>(4) { input.nextInt() },
        price = input.nextInt(),
        tomeIndex = input.nextInt(),
        taxCount = input.nextInt(),
        castable = input.nextInt() != 0,
        repeatable = input.nextInt() != 0
    )
}

data class Inventory(
    val compos: List<Int>,
    val score: Int
) {
    constructor(input: Scanner) : this(
        compos = List<Int>(4) { input.nextInt() },
        score = input.nextInt()
    )

}

fun main() {
    val input = Scanner(System.`in`)

    while (true) {
        val actionCount = input.nextInt() // the number of spells and recipes in play
        val actions = List(actionCount) { Action(input) }

        val myInventory = Inventory(input)
        val oppInventory = Inventory(input)

        log("$myInventory")

        // in the first league: BREW <id> | WAIT; later: BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT
        println("BREW ${actions.sortedBy { it.price }.lastOrNull()!!.id}")
    }
}