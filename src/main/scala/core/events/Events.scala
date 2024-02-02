package core

sealed trait NoteEvent
case class ObjectAdded(id: String) extends NoteEvent
case class ObjectRemoved(id: String) extends NoteEvent
case class ObjectTransformed(id: String) extends NoteEvent
case class ObjectUpdated(id: String) extends NoteEvent
