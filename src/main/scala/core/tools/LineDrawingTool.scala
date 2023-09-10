package core

import java.awt.Graphics2D

import java.awt.geom.AffineTransform

import java.awt.Shape
import java.awt.geom.Path2D



class LineDrawingTool extends Tool:
    @transient private var path: Option[Path2D.Double] = None
    override def accept(visitor: Visitor): Visitor = 
        //TODO clean up else condition useless prolly
        val windowInfo = visitor.windowInfo
        if !visitor.isStopped then
            if path.isEmpty && windowInfo.leftMouseDown then
                path = Some(new Path2D.Double())
                path.foreach(_.moveTo(windowInfo.canvasMousePosition))
                visitor.stopped(InputConsumed(this))

            else if path.isDefined && windowInfo.leftMouseDown then
                path.foreach(_.lineTo(windowInfo.canvasMousePosition))
                visitor.stopped(InputConsumed(this))

            else if path.isDefined && !windowInfo.leftMouseDown then
                val newPath = PathSimplification.fit(path.get.toPointArrayBuffer(), false, 1.5).toPath4()
                path = None
                visitor.canvasObjectManager.add(CanvasShape(newPath), visitor.windowInfo)
                visitor.stopped(InputConsumed(this))
            else
                visitor
        else
            visitor
    
    override def draw(g2d: Graphics2D): Unit = 
        path.foreach(g2d.draw(_))