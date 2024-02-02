package state

import core.*
import java.awt.Graphics2D
import window.WindowInfo
import tools.{LineDrawingTool, LineSelector}
import java.awt.geom.Rectangle2D
import java.awt.Rectangle
import java.awt.event.KeyEvent.*
import java.io.{ObjectOutputStream, FileOutputStream, FileInputStream, ObjectInputStream}
import javax.swing.JButton
import customswingcomponents.WebBrowser

class DefaultToolState(var geometryStore: CanvasObjectManager) extends CanvasState:
    private var currentTool: Option[CanvasObject] = None
    //geometryStore.offer(AsyncCanvasObject(() => {Thread.sleep(1000); CanvasShape(Rectangle(0, 0, 100, 300))}, CanvasShape(Rectangle(0, 0, 1000, 1000))))
    setTool(LineDrawingTool())
    def setTool(tool: CanvasObject): Unit = 
        currentTool = Some(tool)

    

    geometryStore.offer(ExpandingBox.applyThenSubscribe(Rectangle(0, 0, 40, 40), geometryStore), null)
    geometryStore.addComponent(ComponentHandler(new WebBrowser("https://example.com"), Rectangle(200, 200, 400, 400)), null)
    
    def tick(windowInfo: WindowInfo): Unit = 
        if windowInfo(VK_SHIFT, VK_L) then setTool(LineDrawingTool())
        if windowInfo(VK_SHIFT, VK_S) then setTool(LineSelector())
        geometryStore.storeTemp(currentTool.get)
        geometryStore.tick(windowInfo)
        currentTool.foreach(_.tick(windowInfo))
        val foldResult = geometryStore.fold(windowInfo)
        //println(s"Fold result: ${foldResult.stopReason}, ${foldResult.actions}")
        foldResult.actions.foreach {
            case SwapTool(tool) => setTool(tool)
            case RemoveObject(obj) => geometryStore.getStore --= obj
            case Debug => throw Exception("DEBUG")
        }
        
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        geometryStore.draw(g2d, inputInfo)
