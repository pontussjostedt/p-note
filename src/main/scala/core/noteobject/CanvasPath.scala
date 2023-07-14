package core.noteobject

import core.CanvasObject
import java.awt.Graphics2D
import java.awt.Shape
import core.*
import window.WindowInfo
import java.awt.geom.Path2D
import core.Vector2
import scala.collection.mutable.ArrayBuffer
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.event.KeyEvent.*

class CanvasPath(private val path2D: Path2D.Double) extends CanvasObject:
    override def isSafeToCacheInImage: Boolean = true
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
    override def accept(handler: VisitorHandler): VisitorHandler = handler
    override val shape: Shape = path2D
    override def draw(g2d: Graphics2D, input: WindowInfo): Unit =
        g2d.draw(shape)


object CanvasPath:
    def cacheIfNeeded(segmentArray: ArrayBuffer[Segment], color: Color): CanvasPath | ImageCacheDecorator = 
        val path = segmentArray.toPath4()
        val bounds = path.getBounds()
        if segmentArray.length > 6 || bounds.getWidth() * bounds.getHeight() > 10000 then
            ImageCacheDecorator(CanvasPath((path)), color)
        else
            CanvasPath(path)
