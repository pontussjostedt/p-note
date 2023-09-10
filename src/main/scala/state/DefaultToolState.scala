package state
import java.awt.{Graphics2D, Rectangle}
import java.awt.event.KeyEvent.*
import window.*
import core.*
import javax.swing.JLabel
import javax.swing.JButton

class DefaultToolState(canvasObjectManager: CanvasObjectManager) extends CanvasState:

    def acceptInitWithExternalInformation(window: Window): Unit =
        canvasObjectManager.connectWindow(window)
        
    def initTesting(): DefaultToolState = 
        val canvasTestThing = CanvasShape(new java.awt.geom.Ellipse2D.Double(0, 0, 200, 200))
        canvasObjectManager.add(canvasTestThing, null)
        canvasObjectManager.add(ResizeBox(Rectangle(0, 0, 300, 300), canvasObjectManager), null)

        canvasObjectManager.add(CanvasTranslationBox(canvasTestThing, canvasObjectManager), null)
        val swingBridge = canvasObjectManager.addComponentHandler(ComponentHandler(new JButton("Hello World"), Rectangle(400, 400, 200, 200)), null)
        canvasObjectManager.add(CanvasTranslationBox(swingBridge, canvasObjectManager), null)
        this
        
    var tool: Tool = LineDrawingTool()
    override def accept(visitor: Visitor): Visitor = 
        val windowInfo = visitor.windowInfo
        val out: Visitor = tool.accept {
            if visitor.windowInfo(VK_SHIFT, VK_S) then {tool = LineSelector(); visitor.stopped(InputConsumed(this))}
            else if visitor.windowInfo(VK_SHIFT, VK_L) then {tool = LineDrawingTool(); visitor.stopped(InputConsumed(this))}
            else if visitor.windowInfo(VK_SHIFT, VK_ESCAPE) then {tool = NoTool; visitor.stopped(InputConsumed(this))}
            
            else
                visitor
            
        }
        out.foreachCommand {
            case SwapTool(newTool) => tool = newTool
            case _ => ()
        }
            
        

        canvasObjectManager.accept(out)
    override def tick(windowInfo: WindowInfo): Unit = 
        canvasObjectManager.tick(windowInfo)
    override def draw(g2d: Graphics2D): Unit = 
        canvasObjectManager.draw(g2d)
        tool.draw(g2d)
        //canvasObjectManager.draw(g2d)


