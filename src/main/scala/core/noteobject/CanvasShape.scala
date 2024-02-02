package core
import java.awt.{Graphics2D, Shape}
import core.CanvasObject
import window.WindowInfo
case class CanvasShape(awtShape: Shape, override val UUID: String = java.util.UUID.randomUUID().toString()) extends CanvasObject:
    //println(s"CREATED CANVAS SHAPE WITH UUID: $UUID")
    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.draw(awtShape)
    override def tick(input: WindowInfo): Unit = ()
    override def accept(handler: VisitorHandler): VisitorHandler = 
        handler
    override def shape: Shape = awtShape

    override def transformed(transform: Transform): CanvasObject = 
        CanvasShape(transform.createTransformedShape(awtShape), UUID)

