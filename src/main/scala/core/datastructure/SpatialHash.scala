package core

import scala.collection.mutable.{Growable, Shrinkable, Map}

import core.CanvasObject
import java.awt.Rectangle
import java.awt.Shape
import java.awt.Graphics2D
import java.io.ObjectOutputStream
import java.io.ObjectInputStream
sealed trait AnyLayer
sealed trait KnownLayer extends AnyLayer
case class Layer(layer: Int) extends KnownLayer
case object MaxLayer extends KnownLayer
case object TopLayer extends KnownLayer
case object BottomLayer extends KnownLayer

class SpatialHash(private var gridSize: Int) extends Serializable:
    var underlying: Map[(Int, Int), Map[Int, Vector[CanvasObject]]] = Map()

    def addToLayer(elem: CanvasObject, layer: KnownLayer): SpatialHash = 
        val bounds = elem.getBounds
        //println(s"Adding $elem to layer $layer with bounds $bounds")
        val (topX, topY) = hash(bounds.getUpperLeft())
        val (bottomX, bottomY) = hash(bounds.getLowerRight())
        for x <- topX to bottomX; y <- topY to bottomY do
            val layerMap = underlying.getOrElseUpdate((x, y), Map())
            layer match
                case Layer(layer) => 
                    val layerVector = layerMap.getOrElseUpdate(layer, Vector())
                    layerMap.update(layer, layerVector :+ elem)
                case TopLayer =>
                    val topLayer = layerMap.keys.maxOption.getOrElse(10) //Some random number for now will test around a bit before I change anything
                    val layerVector = layerMap.getOrElseUpdate(topLayer, Vector())
                    layerMap.update(topLayer, layerVector :+ elem)
                case MaxLayer => 
                    val layerVector = layerMap.getOrElseUpdate(Int.MaxValue, Vector())
                    layerMap.update(Int.MaxValue, layerVector :+ elem)
                case BottomLayer =>
                    val bottomLayer = layerMap.keys.minOption.getOrElse(0)
                    val layerVector = layerMap.getOrElseUpdate(bottomLayer, Vector())
                    layerMap.update(bottomLayer, layerVector :+ elem)
            
        this

    def removeFromKnownLayer(elem: CanvasObject, layer: Layer): SpatialHash =
        val bounds = elem.getBounds
        val (topX, topY) = hash(bounds.getUpperLeft())
        val (bottomX, bottomY) = hash(bounds.getLowerRight())
        for x <- topX to bottomX; y <- topY to bottomY do
            assert(underlying.contains((x, y)), s"Trying to remove from non-existing cell ($x, $y)")
            assert(underlying((x, y)).contains(layer.layer), s"Trying to remove from non-existing layer ${layer.layer} in cell ($x, $y)")
            underlying(x, y) += layer.layer -> underlying(x, y)(layer.layer).filter(_ != elem)
        this

    def removeFromUnkownLayer(elem: CanvasObject): SpatialHash =
        val bounds = elem.getBounds
        val (topX, topY) = hash(bounds.getUpperLeft())
        val (bottomX, bottomY) = hash(bounds.getLowerRight())
        for x <- topX to bottomX; y <- topY to bottomY do
            assert(underlying.contains((x, y)), s"Trying to remove from non-existing cell ($x, $y)")
            val layerMap: Map[Int, Vector[CanvasObject]] = underlying(x, y)
            underlying.foreach { (k, v) => 
                underlying.update(k, v.map { case (layer, vector) => layer -> vector.filter(_ != elem) })
            }
        this

    infix def +=(elem: CanvasObject): SpatialHash = addToLayer(elem, TopLayer)
    infix def ++=(elems: Iterable[CanvasObject]): SpatialHash = 
        elems.foreach(elem => addToLayer(elem, TopLayer))
        this
    infix def -=(elem: CanvasObject): SpatialHash = removeFromUnkownLayer(elem)
    infix def --= (elems: Iterable[CanvasObject]): SpatialHash = 
        elems.foreach(elem => removeFromUnkownLayer(elem))
        this

    def foreachZone(rect: Rectangle)(f: CanvasObject => Unit): Unit =
        val (topX, topY) = hash(rect.getUpperLeft())
        val (bottomX, bottomY) = hash(rect.getLowerRight())
        for x <- topX to bottomX; y <- topY to bottomY do
            if underlying.contains(x, y) then //Only do it on cells that actually exist
                underlying(x, y).values.flatten.foreach(f)

    def foreachZone(rect: Rectangle)(f: (Int, CanvasObject) => Unit): Unit = 
        val (topX, topY) = hash(rect.getUpperLeft())
        val (bottomX, bottomY) = hash(rect.getLowerRight())
        for x <- topX to bottomX; y <- topY to bottomY do
            if underlying.contains(x, y) then //Only do it on cells that actually exist
                underlying(x, y).foreach { case (layer, vector) => vector.foreach(elem => f(layer, elem)) }
            
    /**
      *
      * @param rectangle rectangle to check for intersections
      * @return All objects that intersects the given rectangle or are contained inside it
      */
    def queryClippingRectangle(rectangle: Rectangle): Set[CanvasObject] =
        val set = Set.newBuilder[CanvasObject]
        foreachZone(rectangle)(elem =>  set += elem)
        set.result().filter(elem => rectangle.optimizedIntersect(elem.shape, 250*250))

    def queryClippingRectanglePreservedLayers(rectangle: Rectangle): scala.collection.immutable.Map[Int, Vector[CanvasObject]] =
        var currentCanvasObjects = Set[CanvasObject]()
        var discarded = Set[CanvasObject]()
        val map = scala.collection.immutable.Map.newBuilder[Int, Vector[CanvasObject]]
        foreachZone(rectangle) { (layer, elem) => 
            if !currentCanvasObjects.contains(elem) && !discarded.contains(elem) then
                if rectangle.g2dScaledOptimizedIntersect(elem.shape) then 
                    currentCanvasObjects += elem
                    map += layer -> (map.result().getOrElse(layer, Vector()) :+ elem)
                else
                    discarded += elem
        }
        map.result()

    def queryClippingShape(shape: Shape): Set[CanvasObject] =
        val set = Set.newBuilder[CanvasObject]
        foreachZone(shape.getBounds) (elem =>  if shape.g2dScaledOptimizedIntersect(elem.shape) then set += elem)
        set.result()

    def queryContainedByShape(shape: Shape): Set[CanvasObject] =
        val set = Set.newBuilder[CanvasObject]
        foreachZone(shape.getBounds) (elem => set += elem)
        set.result().filter(elem => shape.contains(elem.shape))


    def debugDraw(g2d: Graphics2D): Unit =
        underlying.foreach { case ((x, y), layerMap) =>
            g2d.drawRect(x * gridSize, y * gridSize, gridSize, gridSize)
            g2d.drawString(s"($x, $y)", x * gridSize, y * gridSize + 10)
            //layerMap.zipWithIndex.foreach { case ((layer, vector), index) =>
            //   g2d.drawString(s"$layer: ${vector.size}", x * gridSize, y * gridSize + (index + 1) * 10)
            
            //}    
        }

    def clearDeadZones(): Unit = 
        underlying.filterInPlace((_, layerMap) => layerMap.nonEmpty)
        



    

    def hash(num: Int): Int = num / gridSize
    def hash(vector: Vector2): (Int, Int) = (hash(vector.x), hash(vector.y))
    
    private def writeObject(out: ObjectOutputStream): Unit =
        println("Running writeobject in spatialhash")
        out.writeInt(gridSize)
        val immutableUnderlying: scala.collection.immutable.Map[(Int, Int), Map[Int, Vector[CanvasObject]]] = underlying.toMap
        out.writeObject(immutableUnderlying)

    private def readObject(in: ObjectInputStream): Unit = 
        println("Running readobject in spatialhash")
        gridSize = in.readInt()
        val immutableUnderlying = in.readObject().asInstanceOf[scala.collection.immutable.Map[(Int, Int), Map[Int, Vector[CanvasObject]]]]
        underlying = Map()
        underlying ++= immutableUnderlying
    
