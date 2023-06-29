import window.Window
import core.*
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
object Launcher:
    @main
    def run(): Unit = 
        val windowSize = Vector2(500, 500)
        val window = Window("Hello", windowSize)
        val camera = Camera(AffineTransform(), windowSize)
        while !core.Timer(3000, startOnCd = true).isOver do
            window.frame.getInputContext()
            println(window.inputManager.getInputInfo(camera).debugString)
            window.canvas.render(draw, camera)
            


    def draw(g2d: Graphics2D): Unit =
        g2d.drawPoint(Vector2(20, 20), 30)
  

