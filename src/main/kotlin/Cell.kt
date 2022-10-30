import java.awt.Color
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.JPanel

class Cell(private val context: GameUI, private val line: Int, private val row: Int): JPanel(), MouseListener, KeyListener {
    private var isSelected = false
    private val type
        get() = context.model.getCell(line, row)

    private val defaultBorder = BorderFactory.createBevelBorder(0)
    private val cursorInBorder = BorderFactory.createLineBorder(Color.YELLOW, 2)
    private val selectedBorder = BorderFactory.createLineBorder(Color.YELLOW, 10)

    init {
        addMouseListener(this)
        addKeyListener(this)
        isFocusable = true
    }

    fun updateType() {
        background = colorByType(type)
    }

    private val canBeSelected
        get() = !isSelected && type != CellType.BLOCKED && type != CellType.VOID && context.model.gameOn

    fun select() {
        context.clearCellSelection()
        isSelected = true
        border = selectedBorder
        requestFocus()
    }

    fun deselect() {
        isSelected = false
        border = defaultBorder
    }

    override fun mousePressed(e: MouseEvent?) {
        if (canBeSelected) select()
    }

    override fun mouseEntered(e: MouseEvent?) {
        if (canBeSelected) border = cursorInBorder
    }
    override fun mouseExited(e: MouseEvent?) {
        if (canBeSelected) border = defaultBorder
    }

    override fun keyPressed(e: KeyEvent?) {
        if (e == null) return
        val from = Position(line, row)
        val (shiftX, shiftY) = when (e.keyCode) {
            KeyEvent.VK_LEFT, KeyEvent.VK_A -> listOf(-1, 0)
            KeyEvent.VK_RIGHT, KeyEvent.VK_D -> listOf(1, 0)
            KeyEvent.VK_DOWN, KeyEvent.VK_S -> listOf(0, 1)
            KeyEvent.VK_UP, KeyEvent.VK_W -> listOf(0, -1)
            else -> null
        } ?: return
        val to = Position(line + shiftX, row + shiftY)
        if (context.model.tryMove(Position(line, row), to)) {
            context.apply {
                updateCellType(from)
                updateCellType(to)
                selectCell(to)
            }
        }
    }

    override fun mouseClicked(e: MouseEvent?) {}
    override fun mouseReleased(e: MouseEvent?) {}
    override fun keyTyped(e: KeyEvent?) {}
    override fun keyReleased(e: KeyEvent?) {}

    companion object {
        fun colorByType(type: CellType): Color = when (type) {
            CellType.VOID -> Color.WHITE
            CellType.BLOCKED -> Color.BLACK
            CellType.COLOR_A -> Color.RED
            CellType.COLOR_B -> Color.GREEN
            CellType.COLOR_C -> Color.BLUE
        }
    }
}