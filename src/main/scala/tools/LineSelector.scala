package tools
import core.*
import core.CanvasObject
import core.VisitorHandler
import java.awt.Graphics2D
import window.WindowInfo
import java.awt.Shape
import java.awt.geom.Path2D
import window.LeftMouse

class LineSelector extends CanvasObject:
    private var path: Option[Path2D.Double] = None
    private var locked: Boolean = false
    override val reactive: Boolean = true
    override val accepting: Boolean = true
    override def accept(handler: VisitorHandler): VisitorHandler = 
        if handler.stopReason.isEmpty then
            if handler.windowInfo(LeftMouse) then
                if path.isDefined then
                    path.foreach(_.lineTo(handler.windowInfo.canvasMousePosition))
                else if !locked then
                    path = Some(Path2D.Double())
                    path.foreach(_.moveTo(handler.windowInfo.canvasMousePosition))
                handler.copy(stopReason = Some(InputConsumed))
            else if path.isDefined then
                locked = true
                path.foreach(_.closePath())
                val selected = handler.objectManager.getStore.queryContains(path.get).filter(_.selectable)
                path = None
                val out = handler
                    .stopped(InputConsumed)
                    .addedAction(SwapTool(Resize(selected.toVector, LineDrawingTool(), 30)))
                //println(s"outHandler:      actions: ${out.actions}, stopped: ${out.stopReason}")
                out
            else
                locked = false
                handler
        else
            handler
        
    override def tick(input: WindowInfo): Unit = ()

    override def shape: Shape = path.getOrElse(Path2D.Double())

    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.setColor(java.awt.Color.BLACK)
        g2d.draw(shape)
