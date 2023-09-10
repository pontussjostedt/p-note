package core

import core.CanvasObject
import core.Acceptor
import core.Visitor

trait Tool extends Acceptor[Visitor], Drawable
