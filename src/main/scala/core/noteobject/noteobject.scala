package core
import window.WindowInfo
import java.awt.{Graphics2D, Shape}


trait NoteObject:
    def tick(input: WindowInfo): Unit
    def accept(handler: VisitorHandler): VisitorHandler
    val reactive: Boolean = false
trait CanvasObject extends NoteObject:
    def isSafeToCacheInImage: Boolean = false
    def shape: Shape
    def draw(g2d: Graphics2D, input: WindowInfo): Unit
    def transformed(transform: Transform): CanvasObject = this

