package state

import core.*
import java.awt.Graphics2D
import window.WindowInfo
import tools.{LineDrawingTool, LineSelector}
import java.awt.geom.Rectangle2D
import java.awt.Rectangle
import java.awt.event.KeyEvent.*

class DefaultToolState(geometryStore: CanvasObjectManager) extends CanvasState:
    private var currentTool: Option[CanvasObject] = None
    //geometryStore.offer(AsyncCanvasObject(() => {Thread.sleep(1000); CanvasShape(Rectangle(0, 0, 100, 300))}, CanvasShape(Rectangle(0, 0, 1000, 1000))))
    setTool(LineDrawingTool())
    def setTool(tool: CanvasObject): Unit = 
        println(s"Setting tool to $tool")
        currentTool = Some(tool)
    def tick(windowInfo: WindowInfo): Unit = 
        if windowInfo(VK_SHIFT, VK_L) then setTool(LineDrawingTool())
        if windowInfo(VK_SHIFT, VK_S) then setTool(LineSelector())
        geometryStore.storeTemp(currentTool.get)
        geometryStore.tick(windowInfo)
        currentTool.foreach(_.tick(windowInfo))
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        geometryStore.draw(g2d, inputInfo)
