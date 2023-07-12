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
import core.noteobject.CanvasPath
import java.awt.geom.Path2D
object Launcher:
    @main
    def run(): Unit = 
        val windowSize = Vector2(500, 500)
        val camera = Camera(AffineTransform())
        val fpsTimer = FPSMeassure(5000)
        val window = Window("Hello", windowSize)
        //window.add(ComponentHandler(WebBrowser("http://localhost:8888/?token=616161f3b5b3a60589e8c7f9eb3e592450ae2b8372e0a7e6"), Rectangle(100, 100, 800, 800)))
        //window.add(ComponentHandler(WebBrowser("https://mozilla.github.io/pdf.js/web/viewer.html?file=file:///C:/Users/Pontu/OneDrive/Dokument/Funk.pdf"), Rectangle(100, 100, 800, 1200)))
        //window.add(ComponentHandler(PDFViewer("C:/Users/Pontu/OneDrive/Dokument/Funk.pdf"), Rectangle(100, 100, 800, 1200)))
        println("Starting loop")
        var currentState: CanvasState = DefaultToolState(ClampedCanvasObjectManager(SpatialHash[CanvasObject](100, _.shape), camera))
        
        while !core.Timer(3000, startOnCd = true).isOver do
            val windowInfo = window.inputManager.getInputInfo(camera)
            window.inputManager.reset()
            currentState.tick(windowInfo)
            window.canvas.render(g2d => (), {g2d => currentState.draw(g2d, windowInfo)}, camera)
            //window.canvas.render(g2d => (), draw, camera)
            window.updateCamera(camera)
            camera.debugMove(windowInfo, 700)
            fpsTimer.tick()
            

        var (x, y) = (0, 0)




        
  

