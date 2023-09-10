package state

import window.WindowInfo
import java.awt.Graphics2D
import core.*
import window.Window

trait CanvasState extends Acceptor[Visitor]:
    def acceptInitWithExternalInformation(window: Window): Unit
    def tick(windowInfo: WindowInfo): Unit
    def draw(g2d: Graphics2D): Unit
