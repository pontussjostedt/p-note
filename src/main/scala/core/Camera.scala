package core
import java.awt.geom.Rectangle2D
import window.WindowInfo
import java.awt.event.KeyEvent.*
import java.awt.Rectangle

final case class Camera(transform: Transform) extends Acceptor[Visitor]:
    def toCanvas(vector: Vector2): Vector2 = transform.inverseTransform(vector, null).asInstanceOf[Vector2]
    def toWindow(vector: Vector2): Vector2 = transform.transform(vector, null).asInstanceOf[Vector2]
    def centerPoint(windowSize: Vector2): Vector2 = transform.inverseTransform(windowSize/2)
    def boundingBox(windowSize: Vector2): Rectangle = 
        transform.approximateInvertRect(Rectangle(0, 0, windowSize.x, windowSize.y))
    def boundingBox(windowSize: Vector2, padding: Double): Rectangle = 
        transform.approximateInvertRect(Rectangle(0, 0, windowSize.x, windowSize.y)).pad(padding)

    def scaleAroundCenter(windowSize: Vector2, scale: Double): Unit =
        transform.scale(scale, scale)
        transform.translate(centerPoint(windowSize) * (1 - scale))

    override def accept(visitor: Visitor): Visitor =
        debugMove(visitor.windowInfo, 400)
        visitor.stopped(InputConsumed(this))
    def debugMove(input: WindowInfo, translationSpeed: Double): Unit =
        var toTranslate = Vector2(0, 0)
        if input(VK_W) then
            toTranslate = toTranslate + Vector2(0, 1)
        if input(VK_A) then
            toTranslate = toTranslate + Vector2(1, 0)
        if input(VK_S) then
            toTranslate = toTranslate + Vector2(0, -1)
        if input(VK_D) then
            toTranslate = toTranslate + Vector2(-1, 0)

        if input(VK_U) then
            scaleAroundCenter(input.parentSize, 1 - 0.5 * input.deltaTime)

        if input(VK_Y) then
            scaleAroundCenter(input.parentSize, 1 + 0.5 * input.deltaTime)
            

        transform.translate(toTranslate * translationSpeed * input.deltaTime)