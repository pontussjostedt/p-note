package core
import window.WindowInfo
import java.awt.{Graphics2D, Shape}
import java.awt.Rectangle


trait NoteObject extends Serializable:
    val UUID: String = java.util.UUID.randomUUID.toString
    def tick(input: WindowInfo): Unit = ()
    def accept(handler: VisitorHandler): VisitorHandler
    def reactive: Boolean = false
    def accepting: Boolean = false
trait CanvasObject extends NoteObject:
    def isSafeToCacheInImage: Boolean = false
    def shape: Shape
    def selectable: Boolean = true
    def getBounds: Rectangle = shape.getBounds
    def draw(g2d: Graphics2D, input: WindowInfo): Unit
    def transformed(transform: Transform): CanvasObject = this

