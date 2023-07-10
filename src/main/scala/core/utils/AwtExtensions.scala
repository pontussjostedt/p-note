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
extension (shape: Shape)
    def contains(inner: Shape): Boolean =
        shape.contains(inner.getBounds()) && {
            val selfArea = Area(shape)
            val innerArea = Area(inner)
            selfArea.intersect(innerArea)
            innerArea.contains(selfArea.getBounds2D())
        }


extension (path: Path2D)
    def moveTo(vec: Vector2): Unit =
        path.moveTo(vec.x, vec.y)

    def lineTo(vec: Vector2): Unit =
        path.lineTo(vec.x, vec.y)
        
extension (rect: Rectangle2D)
    def pad(padding: Double): Rectangle2D =
        Rectangle2D.Double(rect.getMinX() + padding, rect.getMinY() + padding, rect.getWidth() - padding, rect.getHeight() - padding)
   
    def getMax(): Vector2 =
        Vector2(rect.getMaxX(), rect.getMaxY())


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

        
