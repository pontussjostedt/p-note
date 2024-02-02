package core

import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import java.awt.geom.AffineTransform
import java.awt.Graphics2D
import java.awt.AlphaComposite
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import scala.collection.mutable.ArrayBuffer
import java.awt.Component
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import java.awt.Color
import java.awt.Rectangle
import java.awt.geom.Ellipse2D

type Vector2 = Point2D.Double
object Vector2:
    def apply(x: Double, y: Double): Vector2 =
        Point2D.Double(x, y)
    def apply(tuple2: (Int, Int)): Vector2 =
        Vector2(tuple2._1, tuple2._2)
    
    def zero: Vector2 =
        Vector2(0, 0)

    def one: Vector2 =
        Vector2(1, 1)

    def fromIntTuple(tuple: (Int, Int)): Vector2 =
        Vector2(tuple._1, tuple._2)

extension (i: Double)
    infix def *(other: Vector2): Vector2 = Vector2(i * other.x, i * other.y)
    infix def +(other: Vector2): Vector2 = Vector2(i + other.x, i + other.y)
    infix def -(other: Vector2): Vector2 = Vector2(i - other.x, i - other.y)
    infix def /(other: Vector2): Vector2 = Vector2(i / other.x, i / other.y)

extension (point2D: Vector2)
    infix def +=(other: Vector2): Unit =
        point2D.x += other.getX()
        point2D.y += other.getY()
    infix def -=(other: Vector2): Unit =
        point2D.x -= other.getX()
        point2D.y -= other.getY()
    infix def *=(other: Vector2): Unit =
        point2D.x *= other.getX()
        point2D.y *= other.getY()
    infix def /=(other: Vector2): Unit =
        point2D.x /= other.getX()
        point2D.y /= other.getY()

    infix def +(other: Vector2): Vector2 = 
        Vector2(point2D.getX() + other.getX(), point2D.getY() + other.getY())
    infix def -(other: Vector2): Vector2 = 
        Vector2(point2D.getX() - other.getX(), point2D.getY() - other.getY())
    //infix def *(other: Vector2): Vector2 = 
    //    Vector2(point2D.getX() * other.getX(), point2D.getY() * other.getY())

    infix def +(scalar: Double): Vector2 = 
        Vector2(point2D.getX() + scalar, point2D.getY() + scalar)
    infix def -(scalar: Double): Vector2 = 
        Vector2(point2D.getX() - scalar, point2D.getY() - scalar)
    infix def *(scalar: Double): Vector2 = 
        Vector2(point2D.getX() * scalar, point2D.getY() * scalar)
    infix def /(scalar: Double): Vector2 = 
        Vector2(point2D.getX() / scalar, point2D.getY() / scalar)

    def dot(vec2: Vector2): Double = 
        point2D.getX() * vec2.getX() + point2D.getY() * vec2.getY()

    def normalized: Vector2 = 
        if point2D.getX() == 0 && point2D.getY() == 0 then
            Vector2.zero
        else
            val mag = point2D.mag
            Vector2(point2D.getX()/mag, point2D.getY()/mag)

    def arg: Double = 
        if point2D.getX() < 0 then
            math.atan(point2D.getY()/point2D.getX()) + math.Pi
        else
            math.atan(point2D.getX()/point2D.getY())

    def angleTo(other: Vector2): Double = 
        (other - point2D).arg

    def mag: Double =
        math.hypot(point2D.getX(), point2D.getY())

    def niceString(): String = s"Vector(${point2D.x.toInt}, ${point2D.y.toInt})"
   
  
type Transform = AffineTransform
object Transform:
    def apply(translation: Vector2=Vector2.zero, rotation: Double=0, scale: Vector2=Vector2.one): Transform =
        val out = new AffineTransform()
        out.translate(translation)
        out.rotate(rotation)
        out.scale(scale)
        out

    def identity: Transform = AffineTransform()
extension (affine: AffineTransform)
    
    def getScale(): Vector2 =
        Vector2(affine.getScaleX(), affine.getScaleY())

    def getTranslation(): Vector2 =
        Vector2(affine.getTranslateX(), affine.getTranslateY())

    def scale(vec: Vector2): Unit =
        affine.scale(vec.x, vec.y)
    def setToScale(vec: Vector2): Unit =
        affine.setToScale(vec.x, vec.y)

    def translate(vec: Vector2): Unit =
        affine.translate(vec.x, vec.y)
    
    def setToTranslation(vec: Vector2): Unit =
        affine.setToTranslation(vec.x, vec.y)

    def inverseTransform(vec: Vector2): Vector2 =
        val out = Vector2.zero
        affine.inverseTransform(vec, out)
        out

    def scaleAround(scale: Vector2, anchor: Vector2): Unit =
        affine.scale(scale.x, scale.y)
        affine.translate(anchor.x * (1 - scale.x), anchor.y * (1 - scale.y))
        //transform.scale(scale, scale)
        //transform.translate(centerPoint(windowSize) * (1 - scale))

    def rotate(rotation: Double, vec: Vector2): Unit =
        affine.rotate(rotation, vec.x, vec.y)

    def preTranslate(vec: Vector2): Unit =
        affine.preConcatenate(Transform(vec))

    def preTranslate(x: Double, y: Double): Unit =
        preTranslate(Vector2(x, y))

    def zerodRotation: Transform =
        val out = affine.clone().asInstanceOf[Transform]
        out.setToScale(0, 0)
        out

    def approximateInvertRect(rect: Rectangle2D): Rectangle2D =
            val newAffine = affine.createInverse()
            newAffine.createTransformedShape(rect).getBounds2D()



private given Conversion[Double, Int] = _.toInt
extension (g2d: Graphics2D)
    def clearRect(p1: Vector2, width: Double, height: Double): Unit =
        g2d.clearRect(p1.x, p1.y, width, height)

    def fillOval(topRight: Vector2, width: Double, height: Double): Unit =
        g2d.fillOval(topRight.x, topRight.y, width, height)

    def drawPoint(point: Vector2, radius: Double): Unit =
        val topRight = point - radius
        g2d.fillOval(topRight, radius*2, radius*2)

    def setOpacity(opacity: Float): Unit =
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity))

    def fillRect(rect: Rectangle2D): Unit = g2d.fillRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight())

    def drawLine(p1: Vector2, p2: Vector2): Unit =
        g2d.drawLine(p1.x, p1.y, p2.x, p2.y)

    def drawCrosshair(p: Vector2, radius: Int): Unit =
        g2d.drawLine(p.x - radius, p.y, p.x + radius, p.y)
        g2d.drawLine(p.x, p.y - radius, p.x, p.y + radius)

    def clearRect(p1: Vector2, size: Vector2): Unit =
        g2d.clearRect(p1.x, p1.y, size.x, size.y)

    def fillRect(p1: Vector2, size: Vector2): Unit =
        g2d.fillRect(p1.x, p1.y, size.x, size.y)

    def drawRect(p1: Vector2, size: Vector2): Unit = 
        g2d.drawRect(p1.x, p1.y, size.x, size.y)

    def drawString(text: String, p1: Vector2): Unit =
        g2d.drawString(text, p1.x, p1.y)
extension (shape: Shape)
    def contains(inner: Shape): Boolean =
        {shape.getBounds().contains(inner.getBounds())} && {{
            val selfArea = Area(shape)
            val innerArea = Area(inner)
            selfArea.intersect(innerArea)
            innerArea.contains(selfArea.getBounds2D())
        } || tryContains(shape, inner)}

    def intersects(other: Shape): Boolean =
        val selfBounds = shape.getBounds()
        val otherBounds = other.getBounds()
        selfBounds.intersects(otherBounds) && {
            // TODO: refactor together with try contains so I am not duplicating my code like a crazy man
            val upperPointInImageInner = otherBounds.getUpperLeft() - selfBounds.getUpperLeft()
            val image = BufferedImage(selfBounds.getWidth(), selfBounds.getHeight(), BufferedImage.TYPE_INT_ARGB)
            val g2d = image.createGraphics()
            g2d.translate(-selfBounds.x, -selfBounds.y)
            g2d.setColor(Color.WHITE)
            g2d.fillRect(selfBounds.x, selfBounds.y, selfBounds.width, selfBounds.height)
            g2d.setColor(Color.BLACK)
            g2d.fill(shape)
            val selfPoints = image.getNoneZeroIndices()

            g2d.setColor(Color.WHITE)
            g2d.fillRect(selfBounds.x, selfBounds.y, selfBounds.width, selfBounds.height)
            g2d.setColor(Color.BLACK)
            g2d.draw(other)
            val innerPoints = image.getNoneZeroIndices()
            g2d.dispose()

            innerPoints.exists(selfPoints.contains)
        }

val clocker = CodeClocker()
def tryContains(outer: Shape, inner: Shape): Boolean =
    val bounds = outer.getBounds()
    val innerBounds = inner.getBounds()
    val upperPointInImageInner = innerBounds.getMin() - bounds.getMin()
    val image = BufferedImage(innerBounds.getWidth(), innerBounds.getHeight(), BufferedImage.TYPE_INT_ARGB)
    val g2d = image.createGraphics()
    g2d.translate(-innerBounds.x, -innerBounds.y)
    g2d.setColor(Color.WHITE)
    g2d.fillRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height)
    g2d.setColor(Color.BLACK)
    g2d.fill(outer)
    //g2d.setColor(Color.BLUE)
    //g2d.draw(outer)
    //val outerFile = new File("outer.png")
    //ImageIO.write(image, "png", outerFile)
    val outerPoints = image.getNoneZeroIndices()
    //val overlayedFile = new File("overlayed.png")
    //g2d.setColor(Color.RED)
    //g2d.draw(inner)
    //ImageIO.write(image, "png", overlayedFile)
    g2d.setColor(Color.WHITE)
    g2d.fillRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height)
    g2d.setColor(Color.BLACK)
    g2d.draw(inner)
    val innerPoints = image.getNoneZeroIndices()
    g2d.dispose()
    //val outputfile = new File("inner.png")
    //ImageIO.write(image, "png", outputfile)
    innerPoints.forall(outerPoints.contains)
    
extension (image: BufferedImage)
    def getNoneZeroPoints(): Vector[Vector2] =
        (for x <- 0 until image.getWidth(); y <- 0 until image.getHeight() if {image.getRGB(x, y) & 0x00ffffff} == 0x00000000 yield {Vector2(x, y)}).toVector

    def getNoneZeroIndices(): Vector[Int] =
        image
            .getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())
            .zipWithIndex
            .filter((c, i) => {(c & 0x00ffffff)} == 0x00000000)
            .map(_._2)
            .toVector
    //def getNoneZeroIndices(topLeftPoint: Vector2, size: Vector2): Vector[Int] =
    //    (for (color, i) <- image.getRGB(topLeftPoint.x, topLeftPoint.y, size.x, size.y, null, 0, size.x).zipWithIndex if (color & 0x00ffffff) != 0x00000000 yield i).toVector

extension (path: Path2D)
    def moveTo(vec: Vector2): Unit =
        path.moveTo(vec.x, vec.y)

    def lineTo(vec: Vector2): Unit =
        path.lineTo(vec.x, vec.y)
        
extension (rect: Rectangle2D)
    def pad(padding: Double): Rectangle2D =
        Rectangle2D.Double(rect.getMinX() + padding, rect.getMinY() + padding, rect.getWidth() - padding, rect.getHeight() - padding)
   
    //def getMax(): Vector2 =
    //    Vector2(rect.getMaxX(), rect.getMaxY())

    //def getMin(): Vector2 =
    //    Vector2(rect.getMinX(), rect.getMinY())

extension (rect: Rectangle)
    def getMax(): Vector2 =
        Vector2(rect.getMaxX(), rect.getMaxY())

    def getMin(): Vector2 =
        Vector2(rect.getMinX(), rect.getMinY())

    def getUpperLeft(): Vector2 = Vector2(rect.getMinX(), rect.getMinY())

    def getUpperRight(): Vector2 = Vector2(rect.getMaxX(), rect.getMinY())

    def getLowerLeft(): Vector2 = Vector2(rect.getMinX(), rect.getMaxY())

    def getLowerRight(): Vector2 = Vector2(rect.getMaxX(), rect.getMaxY())

    def cornerMoved(corner: RectangleCorner, vec: Vector2): Rectangle =
        corner match
            case UpperLeft => Rectangle(vec.x, vec.y, rect.width + (rect.x - vec.x), rect.height + (rect.y - vec.y))
            case UpperRight => Rectangle(rect.x, vec.y, (vec.x - rect.x), rect.height + (rect.y - vec.y))
            case LowerLeft => Rectangle(vec.x, rect.y, rect.width + (rect.x - vec.x), (vec.y - rect.y))
            case LowerRight => Rectangle(rect.x, rect.y, (vec.x - rect.x), (vec.y - rect.y))

    def getCorner(corner: RectangleCorner): Vector2 =
        corner match
            case UpperLeft => getUpperLeft()
            case UpperRight => getUpperRight()
            case LowerLeft => getLowerLeft()
            case LowerRight => getLowerRight()

    def getSize: Vector2 =
        Vector2(rect.getWidth(), rect.getHeight())

    def getCenter: Vector2 =
        Vector2(rect.getCenterX(), rect.getCenterY())

    def pad(padding: Double): Rectangle =
        Rectangle(rect.x - padding, rect.y - padding, rect.width + padding*2, rect.height + padding*2)


extension (rect2D: Rectangle2D)
    def getUpperLeft(): Vector2 = Vector2(rect2D.getMinX(), rect2D.getMinY())

    def getUpperRight(): Vector2 = Vector2(rect2D.getMaxX(), rect2D.getMinY())

    def getLowerLeft(): Vector2 = Vector2(rect2D.getMinX(), rect2D.getMaxY())

    def getLowerRight(): Vector2 = Vector2(rect2D.getMaxX(), rect2D.getMaxY())

sealed trait RectangleCorner
case object UpperLeft extends RectangleCorner
case object UpperRight extends RectangleCorner
case object LowerLeft extends RectangleCorner
case object LowerRight extends RectangleCorner

extension (ellipse2D: Ellipse2D)
    def getCenter(): Vector2 =
        Vector2(ellipse2D.getCenterX(), ellipse2D.getCenterY())

object EllipseFactory:
    def asCircle(center: Vector2, radius: Double): Ellipse2D =
        java.awt.geom.Ellipse2D.Double(center.x - radius, center.y - radius, radius*2, radius*2)


extension (component: Component)
    def translateBounds(vec: Vector2): Unit =
        val bounds = component.getBounds()
        bounds.translate(vec.x, vec.y)
        component.setBounds(bounds)


extension (path: Path2D.Double)
    def toPointArrayBuffer(): ArrayBuffer[Vector2] =
        val out = ArrayBuffer[Vector2]()
        val iter = path.getPathIterator(null)
        val coords = Array[Double](0, 0)
        while !iter.isDone() do
            iter.currentSegment(coords)
            out += Vector2(coords(0), coords(1))
            iter.next()
        out

    def curveTo(vec1: Vector2, vec2: Vector2, vec3: Vector2): Unit =
        path.curveTo(vec1.x, vec1.y, vec2.x, vec2.y, vec3.x, vec3.y)

    def lineTo(vec: Vector2): Unit =
        path.lineTo(vec.x, vec.y)

    def moveTo(vec: Vector2): Unit =
        path.moveTo(vec.x, vec.y)

    def quadTo(vec1: Vector2, vec2: Vector2): Unit =
        path.quadTo(vec1.x, vec1.y, vec2.x, vec2.y)


extension (array: ArrayBuffer[Segment])
    def toPath(): Path2D.Double =
        val path = Path2D.Double()
        array.foreach { case Segment(startPoint, handleIn, handleOut) =>
            if handleIn.isEmpty && handleOut.isEmpty then
                if path.getCurrentPoint() == null then
                    path.moveTo(startPoint)
                else
                    path.lineTo(startPoint)
            else
                val handleIn2 = handleIn.getOrElse(startPoint)
                val handleOut2 = handleOut.getOrElse(startPoint)
                
                if path.getCurrentPoint() == null then
                    path.moveTo(startPoint)
                else
                    path.curveTo(handleIn2, handleOut2, startPoint)
            
        }
        path

    def toPath2(): Path2D.Double =
        val path = Path2D.Double()
        array.foreach { case Segment(startPoint, handleIn, handleOut) =>
            if handleIn.isEmpty && handleOut.isEmpty then
                if path.getCurrentPoint() == null then
                    path.moveTo(startPoint)
                else
                    path.lineTo(startPoint)
            else
                val handleIn2 = handleIn.getOrElse(startPoint)
                val handleOut2 = handleOut.getOrElse(startPoint)
                
                if path.getCurrentPoint() == null then
                    path.moveTo(startPoint)
                else
                    path.curveTo(startPoint, handleIn2, handleOut2)
            
        }
        path

    def toPath3(): Path2D.Double =
        val path = Path2D.Double()

        val closed = false
        val preicsion = 5

        if array.nonEmpty then
            val firstSegment = array.head
            path.moveTo(firstSegment.point)
            var prevCord = firstSegment.point
            var out = Vector2.zero

            array.tail.foreach { case Segment(current, handleInOpt, handleOutOpt) => 
                if handleInOpt.isDefined && handleOutOpt.isDefined then
                    val inPoint = current + handleInOpt.get

                    path.curveTo(
                        out - prevCord,
                        inPoint - prevCord,
                        current - prevCord
                    )
                else
                    val dP = current - prevCord
                    
                    if dP.x == 0 then
                        path.lineTo(current)
                    else if dP.y == 0 then
                        path.lineTo(current)
                    else
                        path.lineTo(current-prevCord
                        
                )

                prevCord = current
                out = current + handleOutOpt.getOrElse(Vector2.zero)
            }

        path

    def toPath4(): Path2D.Double =
        val path = Path2D.Double()

        var prevPoint = Vector2.zero
        var outPoint = Vector2.zero

        var first = true
        def addSegment(segment: Segment): Unit =
            val currentPoint = segment.point
            if first then
                path.moveTo(currentPoint)
                first = false
            else
                val inPoint = currentPoint + segment.handleIn.getOrElse(Vector2.zero)
                if inPoint == currentPoint && outPoint == prevPoint then
                    if true then
                        val dp = currentPoint// - prevPoint
                        path.lineTo(dp)
                else
                    path.curveTo(outPoint, inPoint, currentPoint)
            prevPoint = currentPoint
            outPoint = currentPoint + segment.handleOut.getOrElse(Vector2.zero)
            
        array.foreach(addSegment(_))
        path


extension (rectangles: Seq[Rectangle])
    def createUnion: Rectangle =
        assert(rectangles.nonEmpty, "Cannot create union of empty list")
        rectangles.foldLeft(rectangles.head)((a, b) => a.union(b))

extension (rectangles: Seq[Rectangle2D])
    def createUnion: Rectangle2D =
        assert(rectangles.nonEmpty, "Cannot create union of empty list")
        rectangles.foldLeft(rectangles.head)((a, b) => a.createUnion(b))


        
