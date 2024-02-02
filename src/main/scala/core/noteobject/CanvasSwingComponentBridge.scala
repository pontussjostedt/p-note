package core

import core.CanvasObject
import java.awt.Graphics2D
import window.WindowInfo
import java.awt.Shape
import core.VisitorHandler
import core.ComponentHandler
import java.awt.Rectangle

class CanvasSwingComponentBridge(componentHandler: ComponentHandler) extends CanvasObject:
    override def accept(handler: VisitorHandler): VisitorHandler = handler

    override def shape: Shape = componentHandler.getBounds

    override def getBounds: Rectangle = componentHandler.getBounds

    override def transformed(transform: Transform): CanvasObject = 
        val newBounds = transform.createTransformedShape(componentHandler.bounds).getBounds()
        componentHandler.setBounds(newBounds)
        this

    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
      g2d.setColor(java.awt.Color.GREEN)
      g2d.draw(getBounds.pad(15))
      g2d.setColor(java.awt.Color.BLACK)


