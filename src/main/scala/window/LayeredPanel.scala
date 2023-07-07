package window

import javax.swing.JPanel
import javax.swing.JLayeredPane

class LayeredPanel extends JPanel:
    private val layeredPane = JLayeredPane()
    add(layeredPane)
