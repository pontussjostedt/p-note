package core

import java.awt.geom.Rectangle2D
import java.awt.Shape
import scala.collection.mutable.Map
import window.WindowInfo

class SpatialEventEmitter[E, C](val channels: C*) extends Serializable:
    private val listeners = Map.empty[C, Vector[Listener[E, C]]]
    def emitGlobal(event: E)(windowInfo: WindowInfo, objectManager: CanvasObjectManager): Unit = 
        //println("EMITTING GLOBAL")
        listeners.foreach((channel, listeners) => listeners.foreach((listener) => listener.callback(event, windowInfo, objectManager, objectManager.getCanvasObject(listener.uuid).get)))
        

    def emitGlobal(event: E, channels: C*)(windowInfo: WindowInfo, objectManager: CanvasObjectManager): Unit =
        for channel <- channels do
            //println(s"EMITTING GLOBAL: ${channel}")
            //println(listeners.get(channel).mkString("\n"))
            listeners.get(channel).foreach((listeners) => listeners.foreach((listener) => {listener.callback(event, windowInfo, objectManager, objectManager.getCanvasObject(listener.uuid).get)}))

    def emitBounds(event: E, bounds: Rectangle2D)(windowInfo: WindowInfo, canvasObjectManger: CanvasObjectManager): Unit = ???
        //canvasObjectManger.

    def unsubscribe(uuid: String): Unit = 
        listeners.foreach((channel, listeners) => listeners.filter((listener) => listener.uuid != uuid))

    def subscribe(listener: Listener[E, C], channel: C): Unit = 
        listeners += (channel -> (listeners.getOrElse(channel, Vector.empty) :+ listener))

case class Listener[E, C](uuid: String, channel: C, callback: (E, WindowInfo, CanvasObjectManager, CanvasObject) => Unit)
object Listener:
    def apply[E, C](canvasObject: CanvasObject, channel: C, callBack: (E, WindowInfo, CanvasObjectManager, CanvasObject) => Unit): Listener[E, C] = 
        new Listener[E, C](canvasObject.UUID, channel, callBack)



