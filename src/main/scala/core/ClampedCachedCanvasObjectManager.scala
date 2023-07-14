package core

import java.awt.Graphics2D

import window.WindowInfo
import java.awt.geom.Rectangle2D

class ClampedCachedCanvasObjectManager(canvasObjects: GeometryStore[CanvasObject], initalCamera: Camera) extends CanvasObjectManager() {

    private var queried: Vector[CanvasObject] = Vector.empty
    private var tempStore: Vector[CanvasObject] = Vector.empty
    private var lastQueriedRectangle: Option[Rectangle2D] = None
    private var toDraw = Vector.empty[CanvasObject]
    
    private var activeCamera: Camera = initalCamera 

    def getCamera: Camera = activeCamera
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit = 
        (toDraw ++ tempStore).foreach(_.draw(g2d, inputInfo))
        canvasObjects.draw(g2d)
        tempStore = tempStore.empty
    def offer(canvasObject: CanvasObject): Unit = 
        toDraw :+= canvasObject
        canvasObjects += canvasObject

    def tick(input: WindowInfo): Unit =
        val bounds = activeCamera.boundingBox(input.parentSize)
        if lastQueriedRectangle.isEmpty || !lastQueriedRectangle.contains(bounds) then
            lastQueriedRectangle = Some(bounds)
            queried = canvasObjects.queryClippingRect(bounds).toVector
            toDraw = generateDraws(queried)
        getActive.map(_.tick(input))

    def fold(input: WindowInfo): VisitorHandler = 
        getActive.filter(_.reactive).foldLeft[VisitorHandler](VisitorHandler(input, this, Vector.empty, Vector.empty, None)){
            (handler, canvasObject) => canvasObject.accept(handler)
        }
    def getActive: Vector[CanvasObject] = tempStore ++ queried
    private def generateDraws(canvasObjects: Vector[CanvasObject]): Vector[CanvasObject] = 
        val (safe, unsafe) = canvasObjects.partition(_.isSafeToCacheInImage)
        if safe.nonEmpty then
           MultiImageCacheDecorator(safe, java.awt.Color.BLACK) +: unsafe
        else
            unsafe
    def storeTemp(canvasObject: CanvasObject): Unit =
        tempStore :+= canvasObject

    def getStore: GeometryStore[CanvasObject] = canvasObjects


}
