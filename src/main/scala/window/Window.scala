package window

import javax.swing.{JFrame, JButton}
import java.awt.Dimension
import core.*
import javax.swing.JPanel
import java.awt.GridLayout
import java.awt.Canvas
import java.awt.Graphics2D
import java.awt.event.ComponentAdapter

class Window(title: String, var windowSize: Vector2):
    val dim = Dimension(windowSize.getX.toInt, windowSize.getY.toInt)
    val frame: JFrame = JFrame(title: String)
    frame.setSize(dim)
    //frame.setResizable(false)
    frame.setVisible(true)
    val panel = JPanel(GridLayout(2, 1))
    panel.setSize(dim)
    val button = JButton("BOB")
    panel.add(button)
    val canvas = BufferedCanvas(windowSize - Vector2(0, 200))
    panel.add(canvas)
    frame.add(panel)
    val inputManager = InputManager()
    inputManager.attach(canvas)
    frame.pack()

class BufferedCanvas(var canvasSize: Vector2) extends Canvas:
    val dim = Dimension(canvasSize.x.toInt, canvasSize.y.toInt)
    super.setSize(dim)

    addComponentListener(new ComponentAdapter {
        override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
            val newDim = e.getComponent.getSize
            canvasSize = Vector2(newDim.getWidth, newDim.getHeight)
        }
    })
    
    def render(drawFunction: Graphics2D => Unit, camera: Camera): Unit = 
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
        g2d.clearRect(Vector2.zero, canvasSize)
        g2d.drawCrosshair(canvasSize/2, 10)
        g2d.setTransform(camera.transform)
        g2d.setColor(Color.RED)
        g2d.drawPoint(Vector2(0, 0), 30)
        g2d.setColor(Color.BLACK)
        drawFunction(g2d) //RITAR ALLT
        bs.show()
        g2d.dispose()

  

