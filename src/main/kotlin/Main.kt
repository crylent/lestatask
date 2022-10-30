import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        GameUI().isVisible = true
    }
}