package core

import window.WindowInfo


sealed trait VisitorContext
case class Resized(resized: CanvasObject) extends VisitorContext


sealed trait VisitorActionCommand
case class SwapTool(tool: CanvasObject) extends VisitorActionCommand

case object Debug extends VisitorActionCommand
sealed trait StopReason
case object InputConsumed extends StopReason

case class VisitorHandler(
    windowInfo: WindowInfo, 
    objectManager: CanvasObjectManager, 
    actions: Vector[VisitorActionCommand], 
    extraContext: Vector[VisitorContext],
    stopReason: Option[StopReason]
):
    def stopped(stopReason: StopReason): VisitorHandler = copy(stopReason = Some(stopReason))
    def addedAction(action: VisitorActionCommand): VisitorHandler = copy(actions = actions :+ action)
    def isStopped: Boolean = stopReason.isDefined
    override def toString(): String = s"VisitorHandler(actions: $actions, extraContext: $extraContext, stopReason: $stopReason)"