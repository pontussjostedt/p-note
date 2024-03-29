import window.Window
import core.*
import scala.language.dynamics
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
import scala.util.Try
import javax.swing.JButton
import java.awt.Rectangle
import customswingcomponents.WebBrowser
import state.DefaultToolState
import state.CanvasState
import java.awt.event.KeyEvent.*
import scala.collection.mutable.ArrayBuffer
import java.awt.Shape
import java.awt.geom.Path2D
object Launcher:
    @main
    def run(): Unit = 
        val windowSize = Vector2(500, 500)
        val camera = Camera(AffineTransform())
        val fpsTimer = FPSMeassure(5000)
        val window = Window("Hello", windowSize)
        //window.add(ComponentHandler(PDFViewer("C:/Users/Pontu/OneDrive/Dokument/Funk.pdf"), Rectangle(100, 100, 800, 1200)))
        println("Starting loop")
        var currentState: CanvasState = DefaultToolState(ClampedCanvasObjectManager(SpatialHash[CanvasObject](100, _.shape), camera, window))
        val path = new Path2D.Double()
        val start = Vector2(100, 100)
        path.moveTo(start)
        path.lineTo(150, 100)
        path.lineTo(100, 200)

        val transform = Transform()
        transform.translate(-1*start)
        transform.scale(2, 2)

        val transformedPath = transform.createTransformedShape(path)
        def draw(g2d: Graphics2D): Unit = ()
            /*
            g2d.drawCrosshair(Vector2.zero, 10) 
            g2d.draw(path)
            g2d.setColor(java.awt.Color.RED)
            g2d.draw(transformedPath)
            */
        
        while !core.Timer(3000, startOnCd = true).isOver do
            val windowInfo = window.inputManager.getInputInfo(camera)
            window.inputManager.reset()
            currentState.tick(windowInfo)
            window.canvas.render(g2d => (), {g2d => {draw(g2d); currentState.draw(g2d, windowInfo)}}, camera)
            //window.canvas.render(g2d => (), draw, camera)
            window.updateCamera(camera)
            camera.debugMove(windowInfo, 700)
            fpsTimer.tick()
            




        
  

