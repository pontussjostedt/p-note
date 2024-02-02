package core
import java.awt.Shape
import java.awt.Graphics2D
import window.WindowInfo
import window.LeftMouse
import java.awt.geom.AffineTransform
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.awt.event.KeyEvent.*
final case class Resize(selected: Vector[CanvasObject], nextTool: CanvasObject, knobRadius: Double) extends CanvasObject:
    assert(selected.nonEmpty, "You cannot resize an empty list of objects")


    case class CornerKnob(shape: Ellipse2D, corner: RectangleCorner, oppositeCorner: RectangleCorner):
        def contains(point: Vector2): Boolean = shape.contains(point)
        def draw(g2d: Graphics2D): Unit = g2d.draw(shape)
    override val reactive: Boolean = true 
    private var startPos: Option[Vector2] = None
    private var scalingStartPos: Option[Vector2] = None
    private var startOfGrab: Vector[CanvasObject] = selected
    private var startOfGrabBoundsOpt: Option[Rectangle] = None
    private var current: Vector[CanvasObject] = selected 
    private var cornerKnobs: Vector[CornerKnob] = getKnobs(shape)
    override def accepting: Boolean = true

    private def getKnobs(bounds: Rectangle): Vector[CornerKnob] = Vector(
        CornerKnob(EllipseFactory.asCircle(bounds.getUpperLeft(), knobRadius), UpperLeft, LowerRight),
        CornerKnob(EllipseFactory.asCircle(bounds.getUpperRight(), knobRadius), UpperRight, LowerLeft),
        CornerKnob(EllipseFactory.asCircle(bounds.getLowerLeft(), knobRadius), LowerLeft, UpperRight),
        CornerKnob(EllipseFactory.asCircle(bounds.getLowerRight(), knobRadius), LowerRight, UpperLeft)
    )
    var debugRectangle: Option[Rectangle] = None
    override def accept(handler: VisitorHandler): VisitorHandler = 
        def scalingNotInitialized: Boolean = scalingStartPos.isEmpty
        def scalingDrag: Boolean = scalingStartPos.isDefined && handler.windowInfo(LeftMouse)
        var out: VisitorHandler = handler
            .stopped(InputConsumed)
            .addedContext(Resized(current.map(_.UUID)))
        handler match
            case VisitorHandler(windowInfo, objectManager, _, _, None) => 
                //objectManager.getStore --= current
                val hoveredKnobOpt: Option[CornerKnob] = cornerKnobs.find(_.contains(windowInfo.canvasMousePosition))
                if windowInfo(VK_DELETE) then
                    out = out
                        .addedAction(RemoveObject(current))
                        .addedContext(Removed(current.map(_.UUID)))
                        .addedAction(SwapTool(nextTool))
                    
                else if hoveredKnobOpt.isDefined then 
                    val hoveredKnob: CornerKnob = hoveredKnobOpt.get
                    if scalingNotInitialized then
                        scalingStartPos = Some(windowInfo.canvasMousePosition)
                        startOfGrab = current
                        startOfGrabBoundsOpt = Some(shape)


                    else if scalingDrag then
                        val startOfGrabBounds = startOfGrabBoundsOpt.get
                        val boundsFittedToActiveCorner: Rectangle = startOfGrabBounds.cornerMoved(hoveredKnob.corner, windowInfo.canvasMousePosition)
                        debugRectangle = Some(boundsFittedToActiveCorner)
                        current.foreach(objectManager.remove(_, windowInfo))
                        val scale: Vector2 = Vector2(
                            boundsFittedToActiveCorner.getWidth() / startOfGrabBounds.getWidth(),
                            boundsFittedToActiveCorner.getHeight() / startOfGrabBounds.getHeight()
                        )
                        val transform: Transform = Transform()
                        transform.translate(startOfGrabBounds.getCorner(hoveredKnob.oppositeCorner))
                        transform.scale(scale)
                        transform.translate(-1*startOfGrabBounds.getCorner(hoveredKnob.oppositeCorner))
                        //current = startOfGrab.map(_.transformed(transform))
                        current = startOfGrab.map(objectManager.transformed(_, transform, windowInfo))
                        cornerKnobs = getKnobs(boundsFittedToActiveCorner)
                        










                else if shape.getBounds2D().contains(windowInfo.canvasMousePosition) then
                    startOfGrabBoundsOpt = None
                    scalingStartPos = None
                    if startPos.isEmpty && windowInfo(LeftMouse) then
                        startOfGrab = current
                        startPos = Some(windowInfo.canvasMousePosition)
                    else if windowInfo(LeftMouse) then
                        val delta = windowInfo.canvasMousePosition - startPos.get
                        current.foreach(objectManager.remove(_, windowInfo))
                        //current = startOfGrab.map(_.transformed(AffineTransform.getTranslateInstance(delta.x, delta.y)))
                        current = startOfGrab.map(objectManager.transformed(_, AffineTransform.getTranslateInstance(delta.x, delta.y), windowInfo))
                        cornerKnobs = getKnobs(shape)
                    else
                        startOfGrab = current 
                        startPos = None
                else if windowInfo(LeftMouse) then 
                    out = out.addedAction(SwapTool(nextTool))
                //objectManager.getStore ++= current
            case _ =>
        out
    
    override def draw(g2d: Graphics2D, input: WindowInfo): Unit = 
        g2d.drawCrosshair(Vector2.zero, 10)
        g2d.draw(shape)
        cornerKnobs.foreach(_.draw(g2d))
        g2d.setColor(java.awt.Color.RED)
        //debugRectangle.foreach(g2d.draw(_))
    override def tick(input: WindowInfo): Unit = ()


    override def shape: Rectangle = current.map(_.shape.getBounds()).foldLeft(current.head.shape.getBounds())(_ union _)


