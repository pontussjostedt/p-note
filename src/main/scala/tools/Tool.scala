package tools

import core.*
import java.awt.Graphics2D
import java.awt.Shape
import window.*
import java.awt.geom.Path2D
import core.noteobject.CanvasPath
import java.awt.Stroke
import java.awt.BasicStroke

class LineDrawingTool extends CanvasObject {
    val timer = Timer(0)
    private var path: Option[Path2D.Double] = None
    override val reactive: Boolean = true

    override def tick(input: WindowInfo): Unit = ()    
    override def accept(handler: VisitorHandler): VisitorHandler =
        val mousePos = handler.windowInfo.canvasMousePosition
        if handler.windowInfo(LeftMouse) then 
            if path.isDefined then 
                if timer.isOverReset then
                    path.foreach(_.lineTo(mousePos))
            else
                path = Some(Path2D.Double())
                path.foreach(_.moveTo(mousePos))
            handler
                .stopped(InputConsumed)
        else if !handler.windowInfo(LeftMouse) && path.isDefined then
            handler.objectManager.offer(CanvasShape(PathSimplification.fit(path.get.toPointArrayBuffer(), false, 10).toPath4()))
            path = None
            handler
                .stopped(InputConsumed)
        else
            handler 
    
    private val dummyShape = Path2D.Double()
    override def shape: Shape = path.getOrElse(dummyShape) 
    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.draw(shape)

}
