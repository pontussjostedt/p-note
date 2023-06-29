package core
import java.awt.geom.Rectangle2D
import window.WindowInfo
final case class Camera(transform: Transform, windowSize: Vector2):
    def toCanvas(vector2: Vector2): Vector2 = transform.transform(vector2, null).asInstanceOf[Vector2]
    def toWindow(vector: Vector2): Vector2 = transform.inverseTransform(vector, null).asInstanceOf[Vector2]
    def centerPoint: Vector2 = transform.inverseTransform(windowSize/2)
    def boundingBox: Rectangle2D = 
        transform.approximateInvertRect(Rectangle2D.Double(0, 0, windowSize.x, windowSize.y))
    def boundingBox(padding: Int): Rectangle2D = 
        transform.approximateInvertRect(Rectangle2D.Double(padding, padding, windowSize.x - padding * 2, windowSize.y - padding * 2))


    def debugTick(input: WindowInfo): Unit =
        ???