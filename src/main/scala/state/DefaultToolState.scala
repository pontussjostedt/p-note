package state

import core.*
import java.awt.Graphics2D
import window.WindowInfo
import tools.LineDrawingTool
import java.awt.geom.Rectangle2D
import java.awt.Rectangle

class DefaultToolState(geometryStore: CanvasObjectManager) extends CanvasState:
    private var currentTool: Option[CanvasObject] = None
    geometryStore.offer(AsyncCanvasObject(() => {Thread.sleep(1000); CanvasShape(Rectangle(0, 0, 100, 300))}, CanvasShape(Rectangle(0, 0, 1000, 1000))))
    setTool(LineDrawingTool())
    def setTool(tool: CanvasObject): Unit = 
        currentTool = Some(tool)
    def tick(windowInfo: WindowInfo): Unit = 
        geometryStore.storeTemp(currentTool.get)
        geometryStore.tick(windowInfo)
        currentTool.foreach(_.tick(windowInfo))
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        geometryStore.draw(g2d, inputInfo)
