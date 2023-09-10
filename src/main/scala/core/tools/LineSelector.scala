package core

import java.awt.Graphics2D
import java.awt.geom.Path2D

class LineSelector extends Tool:
    var path: Option[Path2D.Double] = None
    override def accept(visitor: Visitor): Visitor = 
        if !visitor.isStopped then
            val windowInfo = visitor.windowInfo
            if path.isEmpty && windowInfo.leftMouseDown then
                path = Some(new Path2D.Double())
                path.foreach(_.moveTo(windowInfo.canvasMousePosition))
                visitor.stopped(InputConsumed(this))
            else if path.isDefined && windowInfo.leftMouseDown then
                path.foreach(_.lineTo(windowInfo.canvasMousePosition))
                visitor.stopped(InputConsumed(this))
            else if path.isDefined && !windowInfo.leftMouseDown then
                val newPath = path.get
                newPath.closePath()
                val selected = visitor.canvasObjectManager.queryContaininedByShape(newPath).filter(_.selectable)
                path = None
                if selected.nonEmpty then
                    visitor
                        .commanded(SwapTool(SelectionBox(selected.toVector)))
                        .stopped(InputConsumed(this))
                else
                    println("nothing selected")
                    visitor.stopped(InputConsumed(this))
                    
            else
                visitor
        else
            visitor

    override def draw(g2d: Graphics2D): Unit = 
        path.foreach(g2d.draw(_))
