package state

import core.*
import java.awt.Graphics2D
import window.WindowInfo
import tools.LineDrawingTool

class DefaultToolState(geometryStore: CanvasObjectManager) extends CanvasState:
    private var currentTool: Option[CanvasObject] = None
    setTool(LineDrawingTool())
    def setTool(tool: CanvasObject): Unit = 
        currentTool = Some(tool)
    def tick(windowInfo: WindowInfo): Unit = 
        geometryStore.storeTemp(currentTool.get)
        geometryStore.tick(windowInfo)
        currentTool.foreach(_.tick(windowInfo))
    def draw(g2d: Graphics2D): Unit = 
        geometryStore.draw(g2d)
