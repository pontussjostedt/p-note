package core

import core.Visitor

object NoTool extends Tool:
    override def accept(visitor: Visitor): Visitor = visitor
    override def draw(g2d: java.awt.Graphics2D): Unit = ()
