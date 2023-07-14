package core

import window.InputManager
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import window.WindowInfo
import java.awt.geom.Path2D
import core.noteobject.CanvasPath
import java.awt.event.KeyEvent.*
trait CanvasObjectManager:
    def getCamera: Camera

    def tick(input: WindowInfo): Unit
    def draw(g2d: java.awt.Graphics2D, inputInfo: WindowInfo): Unit

    def offer(canvasObject: CanvasObject): Unit
    def storeTemp(canvasObject: CanvasObject): Unit

    def getStore: GeometryStore[CanvasObject]

    def fold(windowInfo: WindowInfo): VisitorHandler

class ClampedCanvasObjectManager(canvasObjects: GeometryStore[CanvasObject], initalCamera: Camera) extends CanvasObjectManager {
    private var queried: Vector[CanvasObject] = Vector.empty
    private var tempStore: Vector[CanvasObject] = Vector.empty
    private var lastQueriedRectangle: Option[Rectangle2D] = None
    
    private var activeCamera: Camera = initalCamera 

    def getCamera: Camera = activeCamera
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        getActive.foreach(_.draw(g2d, inputInfo))
        //canvasObjects.draw(g2d)
        tempStore = tempStore.empty
    def offer(canvasObject: CanvasObject): Unit = 
        canvasObjects += canvasObject

    def tick(input: WindowInfo): Unit =
        val bounds = activeCamera.boundingBox(input.parentSize)
        if lastQueriedRectangle.isEmpty || !lastQueriedRectangle.contains(bounds) then
            lastQueriedRectangle = Some(bounds)
            queried = canvasObjects.queryClippingRect(bounds).toVector
        queried = canvasObjects.queryClippingRect(bounds).toVector
        //if input(VK_SHIFT) then println(getActive.size)
        getActive.map(_.tick(input))
        
    def fold(input: WindowInfo): VisitorHandler = 
        getActive.filter(_.reactive).foldLeft[VisitorHandler](VisitorHandler(input, this, Vector.empty, Vector.empty, None)){
            (handler, canvasObject) => {canvasObject.accept(handler)}
        }
    def getActive: Vector[CanvasObject] = tempStore ++ queried
    def storeTemp(canvasObject: CanvasObject): Unit =
        tempStore :+= canvasObject

    def getStore: GeometryStore[CanvasObject] = canvasObjects

}
