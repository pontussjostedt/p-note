package core

import core.CanvasObject
import core.Acceptor
import core.Visitor
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.Shape
import java.awt.Graphics2D
import core.CanvasObjectManager
import core.ObjectTransformedEvent
import core.Listener
import window.WindowInfo
import core.NoteEvent

class ResizeBox private(private var rectangle: Rectangle, private var insideBox: Set[CanvasObject]) extends CanvasObject:
    val clocker = CodeClocker("ResizeBox")
    override def selectable: Boolean = false
    override def shape: Shape = rectangle
    
    override def transform(transform: AffineTransform): CanvasObject = throw Exception("I should not be called")
    
    override def draw(g2d: Graphics2D): Unit = 
        g2d.draw(shape)


    def getSquare(canvasObjectManager: CanvasObjectManager): Rectangle =
        //clocker.offerThenPassThrough { () =>
        canvasObjectManager
            .queryClippingShape(rectangle)
            .toVector
            .filter(_ != this)
            .map(_.getBounds)
            .createUnion
        //}

    def setRectangle(rectangle: Rectangle, canvasObjectManager: CanvasObjectManager, windowInfo: WindowInfo): Unit = 
        if this.rectangle != rectangle then 
            //println(s"old rectangle: ${this.rectangle}")
            //println(s"new rectangle: $rectangle")
            canvasObjectManager.removeFromStoreNoEmit(this)
            this.rectangle = rectangle
            canvasObjectManager.addToStoreNoEmit(this)
            canvasObjectManager.notifyEvent(ObjectTransformedEvent(this), core.Channel.ObjectTransformedChannel, windowInfo)

    def onTransform(transfromEvent: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        transfromEvent match
            case ObjectTransformedEvent(obj) => if obj != this then setRectangle(getSquare(canvasObjectManager), canvasObjectManager, windowInfo)
            case _ =>

    def onAddition(transfromEvent: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        transfromEvent match
            case ObjectAddedEvent(obj) => if obj != this then setRectangle(getSquare(canvasObjectManager), canvasObjectManager, windowInfo)
            case _ =>

    def onRemoval(transfromEvent: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        transfromEvent match
            case ObjectRemovedEvent(obj) => if obj != this then setRectangle(getSquare(canvasObjectManager), canvasObjectManager, windowInfo)
            case _ =>

object ResizeBox:
    def apply(rectangle: Rectangle, canvasObjectManager: CanvasObjectManager): ResizeBox = 
        val resizeBox = new ResizeBox(rectangle, canvasObjectManager.queryContaininedByShape(rectangle))
        canvasObjectManager.subscribe(Listener(resizeBox.onTransform, resizeBox.id), core.Channel.ObjectTransformedChannel)
        canvasObjectManager.subscribe(Listener(resizeBox.onAddition, resizeBox.id), core.Channel.ObjectAddedChannel)
        canvasObjectManager.subscribe(Listener(resizeBox.onRemoval, resizeBox.id), core.Channel.ObjectRemovedChannel)
        resizeBox

