import window.Window
import core.*
import core.Channel.*
import scala.language.dynamics
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
import java.awt.Rectangle
import state.CanvasState
import java.awt.event.KeyEvent.*
import java.awt.Shape
import java.awt.geom.Path2D
import state.*
import scala.util.*
object Launcher:
    @main
    def run(): Unit = 
        val timer: Timer = Timer(1000)
        val windowSize = Vector2(500, 500)
        var camera = Camera(AffineTransform())
        val fpsTimer = FPSMeassure(5000)
        val window = Window("Hello", windowSize)
        println("Starting loop")
        var objectManager: CanvasObjectManager = ClampedCanvasObjectManager(SpatialHash(500), SpatialEventEmitter(Channel.values*), window, camera)
        
        var currentState: CanvasState = DefaultToolState(objectManager).initTesting()


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
            window.updateCamera(camera)
            currentState.tick(windowInfo)

            currentState.accept(Visitor(windowInfo, objectManager, None))
            window.canvas.render(g2d => (), {g2d => {draw(g2d); currentState.draw(g2d)}}, camera)
            if windowInfo(VK_CONTROL, VK_S) && timer.isOverReset then
                println("MAPITIBOOPITY")
                val saver = SimpleProjectSaver()
                saver.writeToDisk(objectManager, "test.txt")
            if windowInfo(VK_CONTROL, VK_L) && timer.isOverReset then
                println("LOADING A CLOCK")
                val loader = SimpleProjectSaver()
                val result = loader.readFromDisk("test.txt")
                result match
                    case Success(value) => {objectManager = value; currentState = DefaultToolState(value); currentState.acceptInitWithExternalInformation(window); camera = value.getCamera; currentState}
                    case Failure(exception) => {exception.printStackTrace(); System.exit(0)}
            fpsTimer.tick()
            




        
  

