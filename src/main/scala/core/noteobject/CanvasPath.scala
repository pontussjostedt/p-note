package core.noteobject

import core.CanvasObject
import java.awt.Graphics2D
import java.awt.Shape
import core.VisitorHandler
import window.WindowInfo
import java.awt.geom.Path2D
import core.Vector2

class CanvasPath(path2D: Path2D.Double) extends CanvasObject:
    def getPathIteratorString: String =
        val pathIterator = path2D.getPathIterator(null)
        val buffer = new StringBuffer()
        while !pathIterator.isDone() do
            val coords = Array[Double](0, 0)
            val segmentType = pathIterator.currentSegment(coords)
            buffer.append(segmentType)
            buffer.append(Vector2(coords(0), coords(1)))
            buffer.append("\n")
            pathIterator.next()
        buffer.append("END OF PATH \n ********* \n ")
        buffer.toString()
    override def tick(input: WindowInfo): Unit = ()
    override def accept(handler: VisitorHandler): Option[VisitorHandler] = Some(handler)  
    override val shape: Shape = path2D
    override def draw(g2d: Graphics2D): Unit =
        g2d.draw(shape)

