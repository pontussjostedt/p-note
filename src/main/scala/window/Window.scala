package window

import javax.swing.{JFrame, JButton}
import java.awt.Dimension
import core.*
import javax.swing.JPanel
import java.awt.GridLayout
import java.awt.Canvas
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter
import javax.swing.JLayeredPane
import java.awt.Panel
import javax.swing.OverlayLayout
import java.awt.Component
import javax.swing.SwingUtilities
import java.awt.BorderLayout
import java.awt.event.MouseListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
class Window(title: String, var windowSize: Vector2):
    private var componentHandlers = Vector.empty[ComponentHandler]
    val dim = Dimension(windowSize.getX.toInt, windowSize.getY.toInt)
    val frame: JFrame = JFrame(title)
    frame.setSize(dim)
    frame.setPreferredSize(dim)
    frame.setVisible(true)

    val canvas = BufferedCanvas(windowSize)
    val inputManager = InputManager(canvas)
    val contentPanel = Layered(windowSize, canvas, inputManager)

    def add(componentHandler: ComponentHandler): Unit = 
        componentHandlers +:= componentHandler
        contentPanel.addToLayer(componentHandler.getComponent, 1)

    def updateCamera(camera: Camera): Unit = 
        componentHandlers.foreach(_.updateCameraInfo(camera))

    inputManager.attach(canvas)
    canvas.setSizeTo(contentPanel)
    contentPanel.setOpaque(true)
    frame.getContentPane().setLayout(new BorderLayout())
    frame.getContentPane().add(contentPanel, BorderLayout.CENTER)
    frame.getContentPane().add(JButton("BOB"), BorderLayout.SOUTH)
    //frame.add(JButton("BOB"))
    frame.pack()

class BufferedCanvas(var canvasSize: Vector2) extends Canvas:
    val dim = Dimension(canvasSize.x.toInt, canvasSize.y.toInt)
    private val selfReference = this

    def setSizeTo(component: Component): Unit = {
        val dim = component.getSize()
        canvasSize = Vector2(dim.getWidth, dim.getHeight)
        component.addComponentListener(new ComponentAdapter {
        override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
            SwingUtilities.invokeLater { () =>
                val newDim = e.getComponent.getSize
                canvasSize = Vector2(newDim.getWidth, newDim.getHeight)
                println(s"Setting size to: ${component.getBounds()}")
                selfReference.setBounds(component.getX(), component.getY(), component.getWidth(), component.getHeight())
            }
        }
        })
        selfReference.setBounds(component.getBounds())
    }
    private val backgroundColor = java.awt.Color.WHITE
    def render(drawAbsolute: Graphics2D => Unit, drawFromCamera: Graphics2D => Unit, camera: Camera): Unit = 
        import java.awt.Color
        var bs = getBufferStrategy
        if (bs == null) {
        createBufferStrategy(3)
        bs = getBufferStrategy
        }
        val g2d = bs.getDrawGraphics().asInstanceOf[Graphics2D]
        g2d.setRenderingHint(
            java.awt.RenderingHints.KEY_ANTIALIASING,
            java.awt.RenderingHints.VALUE_ANTIALIAS_ON
        )
        g2d.setColor(backgroundColor)
        g2d.fillRect(Vector2.zero, canvasSize)
        drawAbsolute(g2d)
        g2d.setTransform(camera.transform)
        g2d.setColor(Color.RED)
        //g2d.drawPoint(Vector2(0, 0), 30)
        g2d.setColor(Color.BLACK)
        drawFromCamera(g2d) //RITAR ALLT
        bs.show()
        g2d.dispose()


class Layered(size: Vector2, canvas: BufferedCanvas, inputManager: InputManager) extends JPanel {
    def addToLayer(component: Component, layer: Int): Unit = 
        layeredPane.add(component, layer, 0)
    

    private val layeredPane = new JLayeredPane()
    layeredPane.setLayout(null) 
    val thing = canvas
    val button2 = new JButton("I AM THE OVERLAY BUTTON") 
    thing.setBounds(0, 0, size.x.toInt, size.y.toInt)
    button2.setBounds(50, 50, 100, 100) 
    layeredPane.add(thing, JLayeredPane.DEFAULT_LAYER, 0)
    //layeredPane.add(button2, JLayeredPane.DEFAULT_LAYER + 1, 0) 
    setLayout(null)
    inputManager.subscribeMouse(button2)
    add(layeredPane)    
    override def doLayout(): Unit = {
      layeredPane.setBounds(0, 0, getWidth, getHeight)
    }
}

    

  

