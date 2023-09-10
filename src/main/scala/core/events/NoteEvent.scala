package core

import core.CanvasObject

sealed trait NoteEvent
case class ObjectAddedEvent(canvasObject: CanvasObject) extends NoteEvent
case class ObjectRemovedEvent(canvasObject: CanvasObject) extends NoteEvent
case class ObjectTransformedEvent(canvasObject: CanvasObject) extends NoteEvent
