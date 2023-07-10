package core

import core.CanvasObject
import java.awt.Shape
import window.WindowInfo
import java.awt.Graphics2D
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.*
case class AsyncCanvasObject(target: () =>  CanvasObject, private var placeholder: CanvasObject) extends CanvasObject {
    private val future = Future[CanvasObject] {
        target()
    }

    private def getOrElse(placeHolder: CanvasObject): CanvasObject =
        val p = future.value
        p match
            case Some(Success(value)) => value
            case None => placeHolder
            case Some(Failure(exception)) => throw exception


    private def current: CanvasObject = getOrElse(placeholder)

    

    override def draw(g2d: Graphics2D): Unit = 
        current.draw(g2d)
    override def tick(input: WindowInfo): Unit = 
        current.tick(input)
    override def accept(handler: VisitorHandler): Option[VisitorHandler] = 
        current.accept(handler)   
    override def shape: Shape = 
        current.shape

}
