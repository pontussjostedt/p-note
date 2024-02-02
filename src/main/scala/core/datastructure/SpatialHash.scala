package core

import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.awt.Graphics2D
import scala.collection.mutable


case class SpatialHash[A](gridSize: Int, getShape: A => Shape) extends GeometryStore[A]:
    private val underlying = mutable.Map.empty[(Int, Int), Vector[A]]

    override def getAll: Seq[A] = underlying.values.flatten.toSet.toSeq
    
    override def queryClippingRect(viewArea: Rectangle2D): Seq[A] = 
        val out = mutable.Set.empty[A]
        forZone(viewArea)(out ++= _)
        out.toSeq

    override def queryClippingShape(selector: Shape): Seq[A] =
        val out = mutable.Set.empty[A]
        forZoneFilter(selector.getBounds2D())(elem => selector.intersects(getShape(elem)), out ++= _)
        out.toSeq

    def forZone(rect: Rectangle2D)(f: (Vector[A]) => Unit): Unit =
        val ((minX, minY), (maxX, maxY)) = getCorners(rect.getBounds2D())
        for x <- minX to maxX; y <- minY to maxY do
            underlying.get((x, y)).foreach(f)

    def getGrids: String = 
        underlying.map((k, v) => k).mkString("\n", "\n", "\n")

    def forZoneFilter(rect: Rectangle2D)(filter: A => Boolean, f: (Vector[A]) => Unit): Unit =
        val ((minX, minY), (maxX, maxY)) = getCorners(rect.getBounds2D())
        for x <- minX to maxX; y <- minY to maxX do
            underlying.get((x, y)).foreach(optVec => f(optVec.filter(filter)))

    override def queryContains(selector: Shape): Seq[A] = 
        val out = mutable.Set.empty[A]
        forZone(selector.getBounds2D())(out ++= _)
        out.filter(elem => selector.contains(getShape(elem))).toSeq

    override def clear(): Unit = underlying.clear()

    override def addOne(elem: A): this.type = 
        val ((minX, minY), (maxX, maxY)) = getCorners(getShape(elem).getBounds2D())
        if elem.isInstanceOf[ExpandingBox] then
            println(s"**** ADDING EXPANDING BOX ****")
        for x <- minX to maxX; y <- minY to maxY do
            pushToGridZone((x, y), elem)
        this

    

    override def subtractOne(elem: A): this.type = 
        val ((minX, minY), (maxX, maxY)) = getCorners(getShape(elem).getBounds2D())
        if elem.isInstanceOf[ExpandingBox] then
            println(s"**** REMOVING EXPANDING BOX ****")
        for x <- minX to maxX; y <- minY to maxY do
            removeFromGridZone((x, y), elem)
        this
    
    private def pushToGridZone(zone: (Int, Int), elem: A): Unit =
        if underlying.isDefinedAt(zone) then
            underlying += zone -> underlying(zone).appended(elem)
        else
            underlying += zone -> Vector(elem)

    //may be iffy check if works
    //Does not remove the key from underlying !TODO
    private def removeFromGridZone(zone: (Int, Int), elem: A): Unit =
        assert(underlying.isDefinedAt(zone), s"You're trying to remove something which does not exist :) \n$elem")
        underlying(zone) = underlying(zone).filter(_ != elem)

    private def removeDeadZones(): Unit =
        underlying.filterInPlace((_, v) => v.length > 0)

    private def hashVector(p: Vector2): (Int, Int) = 
        (hash(p.x), hash(p.y))
    
    private def hash(d: Double): Int =
        (d/gridSize).toInt

    private def getCorners(rec: Rectangle2D): ((Int, Int), (Int, Int)) =
        ((hash(rec.getMinX()), hash(rec.getMinY())), (hash(rec.getMaxX()), hash(rec.getMaxY())))

    override def draw(g2d: Graphics2D): Unit = 
        g2d.setColor(java.awt.Color.RED)
        underlying.foreach { (k, v) =>
            g2d.drawRect(Vector2.fromIntTuple(k) * gridSize, Vector2(gridSize, gridSize))
            g2d.drawString(s"NumContent: ${v.length}", Vector2.fromIntTuple(k) * gridSize)
        }

object SpatialHash:
    def apply[A](gridSize: Int, getShape: A => Shape, elements: A*)(): SpatialHash[A] = 
        new SpatialHash(gridSize, getShape).addAll(elements)