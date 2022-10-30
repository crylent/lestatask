import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingUtilities

private const val GAP = 10

class GameUI: JFrame("Game"), ModelListener {

    val model = GameModel().apply { addListener(this@GameUI) }
    private val cells = mutableListOf<MutableList<Cell>>()

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        extendedState = MAXIMIZED_BOTH
        rootPane.contentPane = JPanel(BorderLayout(GAP, GAP)).apply {
            border = BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP)
            add(createField())
            add(createLeftPanel(), BorderLayout.WEST)
        }
        fillField()
    }

    private fun createLeftPanel() = JPanel().apply {
        layout = FlowLayout().apply {
            add(createRestartButton())
        }
    }

    private fun createRestartButton() = JButton("RESTART").apply {
        addActionListener {
            newGame()
        }
    }

    private fun createField(): JPanel {
        val field = JPanel(GridLayout(FIELD_SIZE + 1, FIELD_SIZE))
        var targetNum = 0
        for (x in 0 until FIELD_SIZE) {
            field.add(JPanel().apply {
                if (model.targetRows.contains(x)) {
                    background = Cell.colorByType(model.goal[targetNum])
                    border = BorderFactory.createLineBorder(null, 30)
                    targetNum += 1
                }
            })
        }
        for (y in 0 until FIELD_SIZE) {
            val line = mutableListOf<Cell>()
            for (x in 0 until FIELD_SIZE) {
                val cell = Cell(this, x, y)
                line.add(cell)
                field.add(cell)
            }
            cells.add(line)
        }
        return field
    }

    fun clearCellSelection() {
        cells.forEach {
            it.forEach { cell ->
                cell.deselect()
            }
        }
    }

    fun updateCellType(position: Position) {
        cells[position.y][position.x].updateType()
    }

    fun selectCell(position: Position) {
        cells[position.y][position.x].select()
    }

    private fun fillField() {
        for (y in 0 until FIELD_SIZE) {
            for (x in 0 until FIELD_SIZE) {
                cells[x][y].updateType()
            }
        }
    }

    private fun newGame() {
        model.createField()
        fillField()
    }

    override fun onFinish() {
        SwingUtilities.invokeLater {
            when (JOptionPane.showConfirmDialog(
                this,
                "Start new game?",
                "Game completed!",
                JOptionPane.YES_NO_OPTION
            )) {
                JOptionPane.YES_OPTION -> { newGame() }
                JOptionPane.NO_OPTION -> { model.stopGame() }
            }
        }
    }
}