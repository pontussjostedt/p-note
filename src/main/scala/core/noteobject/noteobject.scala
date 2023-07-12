package core
import window.WindowInfo
import java.awt.{Graphics2D, Shape}


trait NoteObject:
    def tick(input: WindowInfo): Unit
    def accept(handler: VisitorHandler): Option[VisitorHandler]
    val reactive: Boolean = false
trait CanvasObject extends NoteObject:
    def isSafeToCacheInImage: Boolean = false
    def shape: Shape
    def draw(g2d: Graphics2D, input: WindowInfo): Unit

sealed trait VisitorContext
case class VisitorHandler(toRemove: Vector[CanvasObject], windowInfo: WindowInfo, geometryStore: CanvasObjectManager, extraInfo: VisitorContext*)

