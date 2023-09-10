package core

import java.awt.Shape
import java.awt.Graphics2D
class CanvasShape(private var initialShape: Shape) extends CanvasObject:

    override def shape: Shape = initialShape

    override def transform(transform: Transform): CanvasObject =
        initialShape = transform.createTransformedShape(initialShape)
        this

    override def draw(g2d: Graphics2D): Unit =
        g2d.draw(initialShape)
