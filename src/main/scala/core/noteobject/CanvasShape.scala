package core
import java.awt.{Graphics2D, Shape}
import core.CanvasObject
import window.WindowInfo
case class CanvasShape(awtShape: Shape) extends CanvasObject:
    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.draw(awtShape)
    override def tick(input: WindowInfo): Unit = ()
    override def accept(handler: VisitorHandler): Option[VisitorHandler] = 
        Some(handler)
    override def shape: Shape = awtShape
