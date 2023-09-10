package core

import java.awt.geom.AffineTransform
import java.awt.Shape
import java.awt.Graphics2D
import java.awt.Rectangle

trait Drawable:
    def draw(g2d: Graphics2D): Unit

trait Collidable:
    def collisionDraw(g2d: Graphics2D): Unit

trait CanvasObject extends Drawable, Serializable, Collidable:
    val id = java.util.UUID.randomUUID.toString
    def selectable: Boolean = true
    def accepting: Boolean = false
    def reactive: Boolean = false
    def dead: Boolean = _dead
    private var _dead = false
    def kill: Unit = _dead = true

    def shape: Shape
    def getBounds: Rectangle = shape.getBounds

    /**
      * Mutates the object by applying the transform to it
      * @param transform
      * @return The object itself, OBSERVE no new object is created
      */
    def transform(transform: AffineTransform): CanvasObject

    /**
      * Draws the object in a simpler way, used for collision detection
      *
      * @param g2d GraphicsContext
      */
    def collisionDraw(g2d: Graphics2D): Unit = draw(g2d)
