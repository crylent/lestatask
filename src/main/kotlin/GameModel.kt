const val FIELD_SIZE = 5
const val BLOCKED_CELLS_IN_ROW = 3

class GameModel {
    private val field = MutableList(FIELD_SIZE) { MutableList(FIELD_SIZE) { CellType.VOID } }
    private val allRows = mutableListOf<Int>().apply {
        repeat(FIELD_SIZE) { add(it) }
    }.toList()
    val targetRows = listOf(0, 2, 4)
    private val rowsWithBlockedCells = allRows.toMutableList().apply { removeAll(targetRows) }.toList()

    lateinit var goal: List<CellType>
        private set

    private val counter = mutableListOf(0, 0, 0) // correct cells counter

    var gameOn = false
        private set

    fun stopGame() {
        gameOn = false
    }

    init {
        createField()
    }

    fun createField() { // create new field
        counter.replaceAll { 0 } // reset counter

        // randomize goal
        goal = listOf(CellType.COLOR_A, CellType.COLOR_B, CellType.COLOR_C).shuffled()

        // randomize blocked cells
        val cellsUnprocessed = mutableListOf<Position>()
        rowsWithBlockedCells.forEach { x ->
            val row = mutableListOf<Position>().apply {
                for (y in 0 until FIELD_SIZE) {
                    add(Position(x, y))
                }
            }
            repeat(BLOCKED_CELLS_IN_ROW) {
                row.pullRandom().apply { field[y][x] = CellType.BLOCKED }
            }
            cellsUnprocessed.addAll(row)
        }

        // randomize colored cells
        for (y in 0 until FIELD_SIZE) {
            targetRows.forEach { x ->
                cellsUnprocessed.add(Position(x, y)) // add remaining rows
            }
        }
        listOf(CellType.COLOR_A, CellType.COLOR_B, CellType.COLOR_C).forEach { cellType ->
            repeat(FIELD_SIZE) {
                cellsUnprocessed.pullRandom().apply {
                    field[y][x] = cellType
                    val targetNum = targetRows.indexOf(x)
                    if (targetNum != -1 && goal[targetNum] == cellType) counter[targetNum] += 1
                }
            }
        }
        // other sells are void
        cellsUnprocessed.forEach {
            field[it.y][it.x] = CellType.VOID
        }

        gameOn = true
    }

    fun tryMove(from: Position, to: Position): Boolean {
        val fromType = getCell(from)
        if (fromType == CellType.VOID || fromType == CellType.BLOCKED) return false

        if (to.x < 0 || to.x >= FIELD_SIZE || to.y < 0 || to.y >= FIELD_SIZE) return false
        val toType = getCell(to)
        if (toType != CellType.VOID) return false

        setCell(from, CellType.VOID)
        setCell(to, fromType)

        if (from.x != to.x) { // can be progress to goal
            for (i in targetRows.indices) {
                if (goal[i] != fromType) continue
                counter[i] += when (targetRows[i]) {
                    from.x -> -1
                    to.x -> 1
                    else -> 0
                }
            }
        }
        if (checkForCompletion()) {
            listeners.forEach { it.onFinish() }
        }

        return true
    }

    private fun checkForCompletion(): Boolean {
        counter.forEach {
            if (it != FIELD_SIZE) return false
        }
        return true
    }

    fun getCell(x: Int, y: Int) = field[y][x]
    private fun getCell(position: Position) = field[position.y][position.x]
    private fun setCell(position: Position, type: CellType) {
        field[position.y][position.x] = type
    }

    private val listeners = mutableSetOf<ModelListener>()

    fun addListener(listener: ModelListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ModelListener) {
        listeners.remove(listener)
    }
}

fun<T> MutableCollection<T>.pullRandom(): T { // pull random item from list
    return random().also { remove(it) }
}