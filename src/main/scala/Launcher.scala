import window.Window
import core.*
import scala.language.dynamics
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
import scala.util.Try
import javax.swing.JButton
import java.awt.Rectangle
import customswingcomponents.WebBrowser
import customswingcomponents.PDFViewer
import state.DefaultToolState
import state.CanvasState
import scala.collection.mutable.ArrayBuffer
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
        var currentState: CanvasState = DefaultToolState(ClampedCanvasObjectManager(SpatialHash[CanvasObject](25, _.shape), camera))
        val path2D = new java.awt.geom.Path2D.Double()
        path2D.moveTo(0, 0)
        path2D.lineTo(10, 10)
        path2D.lineTo(23, 24)
        path2D.lineTo(32, 31)
        path2D.lineTo(40, 15);
        path2D.lineTo(55, 5);
        path2D.lineTo(60, 20);
        path2D.lineTo(50, 35);
        path2D.lineTo(100, 100);
        path2D.lineTo(230, 240);
        path2D.lineTo(320, 310);
        path2D.lineTo(400, 150);
        path2D.lineTo(550, 50);
        path2D.lineTo(600, 200);
        path2D.lineTo(500, 350);
        path2D.lineTo(400, 450);
        path2D.lineTo(450, 600);
        path2D.lineTo(750, 350);
        path2D.lineTo(900, 300);
        path2D.lineTo(1050, 350);
        path2D.lineTo(1200, 400);
        path2D.lineTo(1250, 550);
        path2D.lineTo(1150, 700);
        path2D.lineTo(1000, 750);
        path2D.lineTo(850, 800);
        path2D.lineTo(700, 750);
        path2D.lineTo(600, 600);
        path2D.lineTo(650, 450);

        val p = PathSimplification.fit(path2D.toPointArrayBuffer(), false, 1000)
        println(p)
        
        val tan1 = Vector2(-163, -166)
        println(tan1/tan1.mag)
        println(tan1.mag)
        println(math.hypot(tan1.x, tan1.y))
        
        //println(p.mkString("\n"))
        //path2D.lineTo(0, 0)
        //path2D.lineTo(200, 300)

    
        def draw(g2d: Graphics2D): Unit =
            g2d.setColor(java.awt.Color.RED)
            g2d.draw(path2D)
            val answerPath = ArrayBuffer[Segment](Segment(Vector2(0, 0), None, Some(Vector2(10.531334792267995, 10.531334792267995))), Segment(Vector2(32, 31), Some(Vector2(-12.176648471390767, -8.697606050993407)), Some(Vector2(4.131472990238358, 2.951052135884538))), Segment(Vector2(46, 37), Some(Vector2(-3.557225422602663, -3.6226958291536278)), Some(Vector2(52.37048817625579, 53.3343621917697))), Segment(Vector2(200, 200), Some(Vector2(-50.61897788805959, -54.99946635914165)), Some(Vector2(15.961700090698656, 17.34300106008604))), Segment(Vector2(150, 150), Some(Vector2(16.666666666666657, 16.666666666666657)), None))
            g2d.setColor(java.awt.Color.GREEN)
            g2d.draw(p.toPath4())
            /*
            g2d.setColor(java.awt.Color.GREEN)
            g2d.draw(answerPath.toPath())
            g2d.setColor(java.awt.Color.BLUE)
            g2d.draw(answerPath.toPath2())
            g2d.setColor(java.awt.Color.YELLOW)
            g2d.draw(answerPath.toPath3())
            */
            g2d.setColor(java.awt.Color.MAGENTA)
            //g2d.draw(answerPath.toPath4())
            
        
        while !core.Timer(3000, startOnCd = true).isOver do
            val windowInfo = window.inputManager.getInputInfo(camera)
            window.inputManager.reset()
            currentState.tick(windowInfo)
            window.canvas.render(g2d => (), currentState.draw, camera)
            //window.canvas.render(g2d => (), draw, camera)
            window.updateCamera(camera)
            camera.debugMove(windowInfo, 700)
            fpsTimer.tick()
            

        var (x, y) = (0, 0)
        
  

