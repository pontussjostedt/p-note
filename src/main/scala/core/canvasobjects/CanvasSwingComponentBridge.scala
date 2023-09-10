package core.canvasobjects

import core.CanvasObject
import java.awt.geom.AffineTransform
import java.awt.Shape
import java.awt.Graphics2D
import core.ComponentHandler

class CanvasSwingComponentBridge(componentHandler: ComponentHandler) extends CanvasObject:
    override def draw(g2d: Graphics2D): Unit = ()
    override def shape: Shape = componentHandler.getBounds
    override def transform(transform: AffineTransform): CanvasObject = 
        println("TRANSFORMING COMPONENT")
        val newBounds = transform.createTransformedShape(componentHandler.getBounds).getBounds()
        componentHandler.setBounds(newBounds)
        this



