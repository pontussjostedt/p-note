package core

import window.WindowInfo
import scala.collection.mutable.Map
import scala.collection.mutable
import core.Logger.log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class SpatialEventEmitter[E, C](channels: C*) extends Serializable:
    private var listeners: mutable.Map[C, Vector[Listener[E]]] = channels.map((_, Vector[Listener[E]]())).to{mutable.Map}

    /**
      * Subscribes a listener to the given channel
      *
      * @param channel channel to subscribe to
      * @param callback will be called when an event is emitted on the given channel
      */
    def subscribe(listener: Listener[E], channel: C): Unit =
        listeners(channel) = listeners(channel) :+ listener

    /**
      * Removes the given listener from the given channel
      *
      * @param id
      * @param channel channel to remove listener from
      */
    def unsubscribe(id: String, channel: C): Unit = 
        listeners(channel) = listeners(channel).filter(_.id != id)

    /**
      * Removes the given listener from all channels
      *
      * @param id
      */
    def unsubcribe(id: String): Unit = 
        listeners.foreach((_, listeners) => listeners.filter(_.id != id))

    private def writeObject(out: ObjectOutputStream): Unit = 
        val immutableListeners: scala.collection.immutable.Map[C, Vector[Listener[E]]] = listeners.toMap
        out.writeObject(immutableListeners)
  

    // Define a custom deserialization method
    private def readObject(in: ObjectInputStream): Unit = 
        val immutableListeners: scala.collection.immutable.Map[C, Vector[Listener[E]]] = in.readObject().asInstanceOf[scala.collection.immutable.Map[C, Vector[Listener[E]]]]
        listeners = mutable.Map[C, Vector[Listener[E]]]()
        listeners ++= immutableListeners.toVector

  


    /**
      * Emits an event to all listeners on the given channel
      *
      * @param channel
      * @param event
      * @param windowInfo
      */
    def emitGlobal(event: E, channel: C)(windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit =
        listeners(channel).foreach(_(event, windowInfo, canvasObjectManager))


    


case class Listener[E](callback: (E, WindowInfo, CanvasObjectManager) => Unit, id: String) extends Serializable:
    def apply(event: E, windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager): Unit = callback(event, windowInfo, canvasObjectManager)