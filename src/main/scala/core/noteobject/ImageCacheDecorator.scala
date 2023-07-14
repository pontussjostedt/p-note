package core

import window.WindowInfo
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.Shape
import java.awt.event.KeyEvent.VK_CONTROL
import core.ImageCacheDecorator.objectsToImage

class ImageCacheDecorator(canvasObject: CanvasObject, color: Color) extends CanvasObject:
    override val reactive: Boolean = canvasObject.reactive
    override def tick(input: WindowInfo): Unit = canvasObject.tick(input)
    override def accept(handler: VisitorHandler): VisitorHandler = canvasObject.accept(handler)
    override def shape: Shape = canvasObject.shape

    lazy val imageWithCoordinates = ImageCacheDecorator.shapeToImage(shape, color)

    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        if input(VK_CONTROL) then 
            g2d.draw(shape)
        else
            imageWithCoordinates.draw(g2d)
    
case class ImageWithCoordinates(image: BufferedImage, pos: Vector2, size: Vector2):
    def draw(g2d: Graphics2D): Unit =
        g2d.drawImage(image, pos.x.toInt, pos.y.toInt, size.x.toInt, size.y.toInt, null)

class MultiImageCacheDecorator(canvasObjects: Vector[CanvasObject], color: Color) extends CanvasObject:
    assert(canvasObjects.nonEmpty, "You cannot cache an empty list of objects")
    assert(canvasObjects.forall(_.isSafeToCacheInImage), "You cannot cache an object that is not safe to cache")
    override val reactive: Boolean = false
    override def tick(input: WindowInfo): Unit = canvasObjects.foreach(_.tick(input))
    override def accept(handler: VisitorHandler): VisitorHandler = throw Exception("You are not meant to call me")
    override def shape: Shape = throw Exception("You are not meant to call me")


    private lazy val imageWithCoordinates: Option[ImageWithCoordinates] = objectsToImage(canvasObjects, color)

    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        if input(VK_CONTROL) then 
            canvasObjects.foreach(_.draw(g2d, input))
        else
            imageWithCoordinates.foreach(_.draw(g2d))

object ImageCacheDecorator:
    def objectsToImage(canvasObjects: Vector[CanvasObject], color: Color): Option[ImageWithCoordinates] =
        val bounds = canvasObjects.map(_.shape.getBounds()).foldLeft(canvasObjects.head.shape.getBounds())(_ union _)
        if bounds.isEmpty then 
            None
        else
            val image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB)
            val g2d = image.createGraphics()
            g2d.translate(-bounds.x, -bounds.y)
            g2d.setColor(java.awt.Color.RED)
            g2d.addRenderingHints(java.awt.RenderingHints(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON))    
            canvasObjects.foreach(_.draw(g2d, null))
            g2d.dispose()
            Some(ImageWithCoordinates(image, Vector2(bounds.x, bounds.y), Vector2(bounds.width, bounds.height)))

    def shapeToImage(shape: Shape, shapeColour: Color): ImageWithCoordinates =
        val bounds = shape.getBounds()
        val image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        g2d.translate(-bounds.x, -bounds.y)
        g2d.setColor(java.awt.Color.RED)
        g2d.addRenderingHints(java.awt.RenderingHints(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON))    
        g2d.draw(shape)
        g2d.dispose()
        ImageWithCoordinates(image, Vector2(bounds.x, bounds.y), Vector2(bounds.width, bounds.height))