package state

import window.WindowInfo
import java.awt.Graphics2D

trait CanvasState:
    def tick(windowInfo: WindowInfo): Unit
    def draw(g2d: Graphics2D, inputInfo: WindowInfo): Unit
