package core

import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.event.KeyEvent.*
class SelectionBox(selected: Seq[CanvasObject], knobRadius: Double = 10) extends Tool:
    private def selectBox: Rectangle = selected.map(_.getBounds).createUnion
    private var lastPos: Option[Vector2] = None
    private var knobs = getKnobs(selectBox)
    private var inAction: Boolean = false
    case class CornerKnob(shape: Ellipse2D, corner: RectangleCorner, oppositeCorner: RectangleCorner)

    private def getKnobs(bounds: Rectangle): Vector[CornerKnob] = Vector(
        CornerKnob(EllipseFactory.asCircle(bounds.getUpperLeft(), knobRadius), UpperLeft, LowerRight),
        CornerKnob(EllipseFactory.asCircle(bounds.getUpperRight(), knobRadius), UpperRight, LowerLeft),
        CornerKnob(EllipseFactory.asCircle(bounds.getLowerLeft(), knobRadius), LowerLeft, UpperRight),
        CornerKnob(EllipseFactory.asCircle(bounds.getLowerRight(), knobRadius), LowerRight, UpperLeft)
    )

    var debugBounds: Option[Rectangle] = None
    var lastHoveredKnob: Option[CornerKnob] = None
    override def accept(visitor: Visitor): Visitor = 
        var out = visitor
        knobs = getKnobs(selectBox)

        if !visitor.isStopped then
            val windowInfo = visitor.windowInfo
            val newPos = windowInfo.canvasMousePosition

            if windowInfo(VK_BACK_SPACE) then
                selected.foreach(visitor.canvasObjectManager.remove(_, windowInfo))
                out = visitor
                    .commanded(SwapTool(LineSelector()))
                    .stopped(InputConsumed(this))

            else if windowInfo.leftMouseDown then

        
                val hoveredCornerknobOpt = if inAction then lastHoveredKnob else knobs.find(_.shape.contains(newPos))
                if hoveredCornerknobOpt.isDefined then
                    lastHoveredKnob = hoveredCornerknobOpt
                    inAction = true
                    if lastPos.isEmpty then
                        lastPos = Some(windowInfo.canvasMousePosition)
                    else
                        val hoveredCornerknob = hoveredCornerknobOpt.get
                        val boundsFittedToActiveCorner = selectBox.cornerMoved(hoveredCornerknob.corner, newPos)
                        debugBounds = Some(boundsFittedToActiveCorner)
                        if !selectBox.isEmpty() then 
                            val scale = Vector2(
                                boundsFittedToActiveCorner.getWidth() / selectBox.getWidth(),
                                boundsFittedToActiveCorner.getHeight() / selectBox.getHeight()
                            )
                            if scale.noZeros() then 
                                val transform = Transform()
                                transform.translate(selectBox.getCorner(hoveredCornerknob.oppositeCorner))
                                transform.scale(scale)
                                transform.translate(-1*selectBox.getCorner(hoveredCornerknob.oppositeCorner))
                                selected.foreach(canvasObject => visitor.canvasObjectManager.transform(canvasObject, transform, windowInfo))





                else if selectBox.contains(newPos) || inAction then 
                    inAction = true
                    if lastPos.isEmpty then 
                        lastPos = Some(windowInfo.canvasMousePosition)
                    else
                        val delta = newPos - lastPos.get
                        //println(delta)
                        val transform = Transform(delta)
                        selected.foreach(canvasObject => visitor.canvasObjectManager.transform(canvasObject, transform, windowInfo))
                else
                    out = visitor.commanded(SwapTool(LineSelector()))
            else
                inAction = false
                lastHoveredKnob = None
            lastPos = Some(newPos)

        out
    override def draw(g2d: Graphics2D): Unit = 
        g2d.draw(selectBox)
        //debugBounds.foreach(g2d.draw)
        knobs.foreach(knob => g2d.draw(knob.shape))
    
