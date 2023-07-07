package core.events

import java.awt.geom.Rectangle2D
import java.awt.Shape
import scala.collection.mutable.Map
class SpatialEventEmitter[E, C]:
    private val listeners = Map.empty[C, Vector[E => Unit]]
    def emitGlobal(event: E): Unit = 
        listeners.values.flatten.foreach(_(event))
        

    def emitGlobal(event: E, channels: C*): Unit =
        for channel <- channels do
            listeners.get(channel).foreach(listeners => listeners.foreach(_(event)))


class SpatialListener[E, C](val channels: C*)

    //def emitBounds(event: E, bounds: Rectangle2D): Unit
    //def emitBounds(event: E, bounds: Rectangle2D, channels: C*): Unit

    //def emit(event: E, shape: Shape): Unit
    //def emit(event: E, shape: Shape, channels: C*): Unit


