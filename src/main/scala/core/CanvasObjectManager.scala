package core

import window.InputManager
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import window.WindowInfo
import java.awt.geom.Path2D
import java.awt.event.KeyEvent.*
import java.awt.Shape
import java.io.Serializable
import window.Window
trait CanvasObjectManager extends Serializable:
    def addComponentHandler(component: ComponentHandler, windowInfo: WindowInfo): CanvasSwingComponentBridge = 
        val bridge = CanvasSwingComponentBridge(component)
        offer(bridge, windowInfo)
        bridge
    def getCamera: Camera

    def addComponent(component: ComponentHandler, windowInfo: WindowInfo): CanvasSwingComponentBridge

    def tick(input: WindowInfo): Unit
    def draw(g2d: java.awt.Graphics2D, inputInfo: WindowInfo): Unit

    def getCanvasObject(uuid: String): Option[CanvasObject]

    def subscribe(listener: Listener[NoteEvent, EventChannels], channel: EventChannels): Unit

    def offer(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit
    def transformed(canvasObject: CanvasObject, transform: Transform, windowInfo: WindowInfo): CanvasObject

    def updated(old: CanvasObject, updated: CanvasObject, windowInfo: WindowInfo): Unit
    def updated(old: CanvasObject, updated: CanvasObject, windowInfo: WindowInfo, eventFunction: CanvasObject => NoteEvent, channel: EventChannels): Unit
    def update[A <: CanvasObject](canvasObject: A, windowInfo: WindowInfo, eventFunction: A => NoteEvent, channel: EventChannels, f: A => A): A
    def remove(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit

    def storeTemp(canvasObject: CanvasObject): Unit

    def getStore: GeometryStore[CanvasObject]

    def fold(windowInfo: WindowInfo): VisitorHandler

    def queryContains(selector: Shape): Vector[CanvasObject]

    def queryClippingRect(rectangle: Rectangle2D): Vector[CanvasObject]
class ClampedCanvasObjectManager(canvasObjects: GeometryStore[CanvasObject], initalCamera: Camera, private val window: Window) extends CanvasObjectManager {

    override def addComponent(component: ComponentHandler, windowInfo: WindowInfo): CanvasSwingComponentBridge = 
        val bridge = CanvasSwingComponentBridge(component)
        offer(bridge, windowInfo)
        window.add(component)
        bridge

    override def update[A <: CanvasObject](canvasObject: A, windowInfo: WindowInfo, eventFunction: A => NoteEvent, channel: EventChannels, f: A => A): A = 
        println("GOT HERE IN UPDATE")
        canvasObjects -= canvasObject
        println("GOT HERE IN UPDATE 2")
        val out = f(canvasObject)
        assert(out.UUID == canvasObject.UUID, s"UUID of input object: ${canvasObject.UUID}, UUID of output object: ${out.UUID}")
        canvasObjects += out
        eventEmitter.emitGlobal(eventFunction(out), channel)(windowInfo, this)
        out

    override def updated(old: CanvasObject, updated: CanvasObject, windowInfo: WindowInfo, eventFunction: CanvasObject => NoteEvent, channel: EventChannels): Unit =
        needToQueryAgain = true
        assert(old.UUID == updated.UUID, s"UUID of input object: ${old.UUID}, UUID of output object: ${updated.UUID}")
        println(s"UUID of input object: ${old.UUID}, UUID of output object: ${updated.UUID}")
        //println(s"Rectangle of input object: ${old.shape.getBounds2D()}, Rectangle of output object: ${updated.shape.getBounds2D()}")
        canvasObjects -= old
        canvasObjects += updated
        eventEmitter.emitGlobal(eventFunction(updated), channel)(windowInfo, this)

    private val eventEmitter = SpatialEventEmitter[NoteEvent, EventChannels]()
    private var idMap: Map[String, CanvasObject] = Map.empty  
    private var needToQueryAgain = true




    override def updated(old: CanvasObject, updated: CanvasObject, windowInfo: WindowInfo): Unit = 
        needToQueryAgain = true
        assert(old.UUID == updated.UUID, s"UUID of input object: ${old.UUID}, UUID of output object: ${updated.UUID}")
        canvasObjects -= old
        canvasObjects += updated
        idMap -= old.UUID
        idMap += (updated.UUID -> updated)
        eventEmitter.emitGlobal(ObjectUpdated(updated.UUID), UpdatedChannel)(windowInfo, this)

    override def subscribe(listener: Listener[NoteEvent, EventChannels], channel: EventChannels): Unit =
        eventEmitter.subscribe(listener, channel)

    override def getCanvasObject(uuid: String): Option[CanvasObject] = 
        //println(idMap.mkString("\n"))
        //println(uuid)
        idMap.get(uuid)
    override def queryContains(selector: Shape): Vector[CanvasObject] = canvasObjects.queryContains(selector).toVector

    override def queryClippingRect(rectangle: Rectangle2D): Vector[CanvasObject] = canvasObjects.queryClippingRect(rectangle).toVector

    override def offer(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit = 
        needToQueryAgain = true
        canvasObjects += (canvasObject)
        idMap += (canvasObject.UUID -> canvasObject)
        eventEmitter.emitGlobal(ObjectAdded(canvasObject.UUID), AddedChannel)(windowInfo, this)

    override def transformed(canvasObject: CanvasObject, transform: Transform, windowInfo: WindowInfo): CanvasObject = 
        needToQueryAgain = true
        canvasObjects -= canvasObject
        val out = canvasObject.transformed(transform)
        assert(out.UUID == canvasObject.UUID, s"UUID of input object: ${canvasObject.UUID}, UUID of output object: ${out.UUID}")
        
        canvasObjects += out
        //println(s"UUID of input object: ${canvasObject.UUID}, UUID of output object: ${out.UUID}")
        idMap += (out.UUID -> out)
        //println("Before emit Transform")
        eventEmitter.emitGlobal(ObjectTransformed(out.UUID), TransformChannel)(windowInfo, this)
        out


    override def remove(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit = 
        needToQueryAgain = true
        canvasObjects -= canvasObject
        idMap -= canvasObject.UUID
        eventEmitter.unsubscribe(canvasObject.UUID)
        eventEmitter.emitGlobal(ObjectRemoved(canvasObject.UUID), RemovedChannel)(windowInfo, this)



    private var queried: Vector[CanvasObject] = Vector.empty
    private var tempStore: Vector[CanvasObject] = Vector.empty
    private var lastQueriedRectangle: Option[Rectangle2D] = None
    
    private var activeCamera: Camera = initalCamera 

    def getCamera: Camera = activeCamera
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        getActive.foreach(_.draw(g2d, inputInfo))
        //canvasObjects.draw(g2d)
        tempStore = tempStore.empty

    def tick(input: WindowInfo): Unit =
        val bounds = activeCamera.boundingBox(input.parentSize)
        if needToQueryAgain || lastQueriedRectangle.isEmpty || !lastQueriedRectangle.contains(bounds) then
            needToQueryAgain = false
            lastQueriedRectangle = Some(bounds)
            queried = canvasObjects.queryClippingRect(bounds).toVector
        queried = canvasObjects.queryClippingRect(bounds).toVector
        if input(VK_SHIFT) then println(getActive.size)
        getActive.map(_.tick(input))
        
    def fold(input: WindowInfo): VisitorHandler = 
        getActive.filter(_.accepting).foldLeft[VisitorHandler](VisitorHandler(input, this, Vector.empty, Vector.empty, None)){
            (handler, canvasObject) => {canvasObject.accept(handler)}
        }
    def getActive: Vector[CanvasObject] = tempStore ++ queried
    def storeTemp(canvasObject: CanvasObject): Unit =
        tempStore :+= canvasObject

    def getStore: GeometryStore[CanvasObject] = canvasObjects

}
