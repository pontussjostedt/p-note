package core
import java.awt.Graphics2D
import window.WindowInfo
import java.awt.Shape
import core.canvasobjects.CanvasSwingComponentBridge
import window.Window

trait CanvasObjectManager extends Serializable:
    def addComponentHandler(componentHandler: ComponentHandler, windowInfo: WindowInfo, f: CanvasObject => CanvasObject = x => x): CanvasObject
      /**
      * Adds a new object to the canvas, and emits an event for it
      * @param ob
      */
    def add(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit
    /**
      * Removes an object from the canvas, and emits an event for it
      *
      * @param canvasObject
      */
    def remove(canvasObject: CanvasObject, windowInfo: WindowInfo): Unit

    /**
      * Transforms an object, and emits an event for it
      *
      * @param canvasObject
      * @param transform
      */
    def transform(canvasObject: CanvasObject, transform: Transform, windowInfo: WindowInfo): Unit

    def notifyEvent(event: NoteEvent, channel: Channel, windowInfo: WindowInfo): Unit
    def connectWindow(window: Window): Unit
    def tick(windowInfo: WindowInfo): Unit
    def draw(g2d: Graphics2D): Unit
    def updateThenEmitEvent[A <: CanvasObject](canvasObject: A, f: A => Unit, windowInfo: WindowInfo, event: NoteEvent, channel: Channel): Unit
    def queryContaininedByShape(selector: Shape): Set[CanvasObject]
    def queryClippingShape(selector: Shape): Set[CanvasObject]
    def getCamera: Camera

    def removeFromStoreNoEmit(canvasObject: CanvasObject): Unit
    def addToStoreNoEmit(canvasObject: CanvasObject): Unit

    def accept(a: Visitor): Visitor

    def subscribe(listener: Listener[NoteEvent], channels: Channel*): Unit
