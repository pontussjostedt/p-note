package core

import java.awt.Rectangle
import java.awt.Graphics2D
import window.WindowInfo
import java.awt.Shape
import scala.collection.mutable

class ExpandingBox private(var rectangle: Rectangle, private val ids: mutable.Set[String], UUID: String = java.util.UUID.randomUUID.toString) extends CanvasObject: 
    
    override def selectable: Boolean = false
    override val reactive: Boolean = true

    private val padding = 10

    private def updatedIds(canvasObjectManager: CanvasObjectManager): Set[String] =
        canvasObjectManager.queryContains(rectangle).map((canvasObject) => canvasObject.UUID).toSet
    override def tick(input: WindowInfo): Unit = ()

    override def accept(handler: VisitorHandler): VisitorHandler = handler

    override def shape: Rectangle = rectangle

    override def getBounds: Rectangle = rectangle


    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.draw(shape)

    /**
      * Will have a bug if the object is moved out side the rectangle without intersecting :)
      *
      * @param event
      * @param windowInfo
      * @param canvasObjectManager
      */
    def offerTransformedEvent(event: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        event match
            case event@ObjectTransformed(id) => 
                if id != UUID then
                    val canvasObject = canvasObjectManager.getCanvasObject(event.id).get
                    def updateRectangle: Unit = 
                        val content = canvasObjectManager.queryClippingRect(getBounds).filter(obj => UUID != obj.UUID && this != obj)
                        if !content.isEmpty then
                            val newRectangle = content.map(_.getBounds).createUnion.pad(padding)
                            if newRectangle != rectangle then
                               canvasObjectManager.update[ExpandingBox](
                                this, 
                                windowInfo, 
                                (canvasObject) => ObjectTransformed(canvasObject.UUID),
                                TransformChannel,
                                (canvasObject) => {
                                    rectangle = newRectangle
                                    this
                                }
                            ) 
                        
                    inline def movedOutSideRectangle: Boolean = ids.contains(canvasObject.UUID) && !getBounds.contains(canvasObject.getBounds)
                    if movedOutSideRectangle then
                        updateRectangle
                    else if !ids.contains(canvasObject.UUID) && rectangle.intersects(canvasObject.shape) then
                        if canvasObject.UUID != this.UUID then
                            ids += canvasObject.UUID
                        rectangle = rectangle.union(canvasObject.getBounds)
                        canvasObjectManager.updated(canvasObject, canvasObject, windowInfo, (canvasObject) => ObjectTransformed(canvasObject.UUID), TransformChannel)
                    else
                        updateRectangle
            case _ => throw Exception(s"Something did go wrong in Transformed: ${event}")


    def offerAddedEvent(event: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        event match
            case event@ObjectAdded(id) => 
                val canvasObject = canvasObjectManager.getCanvasObject(event.id).get
                    if !canvasObject.getBounds.isEmpty() && rectangle.intersects(canvasObject.shape) then
                        if canvasObject.UUID != this.UUID then
                            ids += canvasObject.UUID
                            canvasObjectManager.update[ExpandingBox](
                                this, 
                                windowInfo, 
                                (canvasObject) => ObjectTransformed(canvasObject.UUID),
                                TransformChannel,
                                (canvasObject) => {
                                    rectangle = rectangle.union(canvasObject.getBounds).pad(padding)
                                    this
                                }
                            )
            case _ => throw Exception(s"Something did go wrong in Added ${event}")
object ExpandingBox:
    /**
      * Creates an expanding box from a rectangle and a canvas object manager
      * @param rectangle
      * @param canvasObjectManager
      * @return
      */
    def apply(rectangle: Rectangle, canvasObjectManager: CanvasObjectManager): ExpandingBox = 
        val ids = canvasObjectManager.queryContains(rectangle).map((canvasObject) => canvasObject.UUID).to{mutable.Set}
        new ExpandingBox(rectangle, ids)

    def applyThenSubscribe(rectangle: Rectangle, canvasObjectManager: CanvasObjectManager): ExpandingBox =
        val out = apply(rectangle, canvasObjectManager)
        def transformOffer(event: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager, canvasObject: CanvasObject): Unit = 
            //println(s"Offering event: $event to $out")
            // will genreate a bug in a bit!!! LOOK AT ME
            out.offerTransformedEvent(event, windowInfo, canvasObjectManager)

        def addOffer(event: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager, canvasObject: CanvasObject): Unit = 
            out.offerAddedEvent(event, windowInfo, canvasObjectManager)
        
        canvasObjectManager.subscribe(Listener(out.UUID, TransformChannel, transformOffer), TransformChannel)
        canvasObjectManager.subscribe(Listener(out.UUID, AddedChannel, addOffer), AddedChannel)
        out