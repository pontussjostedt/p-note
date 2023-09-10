package core

import core.CanvasObject
import core.Acceptor
import core.Visitor
import java.awt.geom.AffineTransform
import java.awt.Shape
import java.awt.Graphics2D
import java.awt.Rectangle
import window.WindowInfo

class CanvasTranslationBox private(target: CanvasObject, height: Double = 10) extends CanvasObject, Acceptor[Visitor]:
    override def accepting: Boolean = true
    def targetBounds = target.getBounds
    override def selectable: Boolean = false
    private var startPos: Option[Vector2] = None
    private var active = false
    
    override def shape: Rectangle = Rectangle(targetBounds.x, targetBounds.y - height, targetBounds.width, height)
    
    override def transform(transform: AffineTransform): CanvasObject = throw Exception("I should not be called")
    
    override def accept(visitor: Visitor): Visitor = 
        if !visitor.isStopped then
            val windowInfo = visitor.windowInfo
            val rectangle = shape
            if windowInfo.leftMouseDown then 
                if active || rectangle.contains(windowInfo.canvasMousePosition) then
                    if startPos.isEmpty then
                        active = true
                        startPos = Some(windowInfo.canvasMousePosition)
                    else
                        val delta = windowInfo.canvasMousePosition - startPos.get
                        val transform = Transform(delta)
                        visitor.canvasObjectManager.transform(target, transform, windowInfo)
                        startPos = Some(windowInfo.canvasMousePosition)
                visitor.stopped(InputConsumed(this))
            else
                active = false
                startPos = None
                visitor
        else
            visitor

    def onDeletion(transfromEvent: NoteEvent, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = 
        transfromEvent match
            case ObjectRemovedEvent(obj) => 
                if obj == target then 
                    kill
                    canvasObjectManager.remove(this, windowInfo)
            case _ =>

    override def draw(g2d: Graphics2D): Unit = 
        g2d.draw(shape)


object CanvasTranslationBox:
    def apply(target: CanvasObject, canvasObjectManager: CanvasObjectManager, height: Double = 10): CanvasTranslationBox =
        val box = new CanvasTranslationBox(target, height)
        canvasObjectManager.subscribe(Listener(box.onDeletion, box.id), Channel.ObjectRemovedChannel)
        box


