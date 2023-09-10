package core

import java.awt.Graphics2D
import window.WindowInfo
import java.awt.Rectangle
import core.Channel.*
import java.awt.Shape
import core.canvasobjects.CanvasSwingComponentBridge
import window.Window
case class ViewQueryResult(objects: Map[Int, Vector[CanvasObject]], rectangle: Rectangle):
    lazy val sortedObjectsInRectangle: Vector[CanvasObject] = objects.toVector.sortBy(_._1).flatMap(_._2)
    lazy val acceptorsInRectangle: Vector[Acceptor[Visitor]] = sortedObjectsInRectangle.filter(_.accepting).map(_.asInstanceOf[Acceptor[Visitor]])
class ClampedCanvasObjectManager(geometryStore: SpatialHash, eventEmitter: SpatialEventEmitter[NoteEvent, Channel], @transient var window: Window, camera: Camera, cameraPadding: Double = 100) extends CanvasObjectManager:

    val clocker = CodeClocker("ClampedCanvasObjectManager")
    private var lastQuery: Option[ViewQueryResult] = None 
    @transient private var needsQuery: Boolean = true
    println(s"Needsquery: $needsQuery")

    def getCamera: Camera = camera

    override def connectWindow(window: Window): Unit = 
        this.window = window
    def addComponentHandler(componentHandler: ComponentHandler, windowInfo: WindowInfo, f: CanvasObject => CanvasObject): CanvasObject = 
        val bridge: CanvasObject = f(CanvasSwingComponentBridge(componentHandler))
        println(bridge.getBounds)
        geometryStore += bridge
        window.add(componentHandler)
        eventEmitter.emitGlobal(ObjectAddedEvent(bridge), ObjectAddedChannel)(windowInfo, this)
        bridge

    def removeFromStoreNoEmit(canvasObject: CanvasObject): Unit =
        geometryStore -= canvasObject
    def addToStoreNoEmit(canvasObject: CanvasObject): Unit =
        geometryStore += canvasObject
    
    def updateThenEmitEvent[A <: CanvasObject](canvasObject: A, f: A => Unit, windowInfo: WindowInfo, event: NoteEvent, channel: Channel): Unit = 
        geometryStore -= canvasObject
        f(canvasObject)
        geometryStore += canvasObject
        eventEmitter.emitGlobal(event, channel)(windowInfo, this) 

    def notifyEvent(event: NoteEvent, channel: Channel, windowInfo: WindowInfo): Unit = 
        eventEmitter.emitGlobal(event, channel)(windowInfo, this)

    def queryContaininedByShape(selector: Shape): Set[CanvasObject] =
        geometryStore.queryContainedByShape(selector)
        //geometryStore.quer

    def queryClippingShape(selector: Shape): Set[CanvasObject] =
        geometryStore.queryClippingShape(selector)
    def add(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit = 
        geometryStore += canvasObject
        eventEmitter.emitGlobal(ObjectAddedEvent(canvasObject), ObjectAddedChannel)(windowInfo, this)
        needsQuery = true
    def remove(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit = 
        geometryStore -= canvasObject
        eventEmitter.emitGlobal(ObjectRemovedEvent(canvasObject), ObjectRemovedChannel)(windowInfo, this)
        needsQuery = true
    def transform(canvasObject: CanvasObject, transform: Transform, windowInfo: WindowInfo): Unit = 
        geometryStore -= canvasObject
        geometryStore += canvasObject.transform(transform)
        eventEmitter.emitGlobal(ObjectTransformedEvent(canvasObject), ObjectTransformedChannel)(windowInfo, this)
        needsQuery = true

    def tick(windowInfo: WindowInfo): Unit = 
        val cameraRectangle: Rectangle = camera.boundingBox(windowInfo.parentSize, cameraPadding)
        if needsQuery || lastQuery.isEmpty || !lastQuery.get.rectangle.contains(cameraRectangle) then //if should query viewbox
            needsQuery = false
            lastQuery = Some(ViewQueryResult(geometryStore.queryClippingRectanglePreservedLayers(cameraRectangle), cameraRectangle))

        //clocker.offerThenPassThrough(() => geometryStore.clearDeadZones())
            

    def draw(g2d: Graphics2D): Unit = 
        geometryStore.debugDraw(g2d)
        lastQuery.foreach(_.sortedObjectsInRectangle.foreach(_.draw(g2d)))
    def accept(visitor: Visitor): Visitor = 
        if lastQuery.isEmpty then
            camera.accept(visitor)
        else
            (lastQuery.get.acceptorsInRectangle :+ camera).foldLeft(visitor)((acc, acceptor) => acceptor.accept(acc))

    override def subscribe(listener: Listener[NoteEvent], channels: Channel*): Unit = 
        channels.foreach(channel => eventEmitter.subscribe(listener, channel))
