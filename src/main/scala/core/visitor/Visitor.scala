package core

import window.WindowInfo

sealed trait StopReason
case class InputConsumed(consumer: Acceptor[Visitor]) extends StopReason

sealed trait Command
case class SwapTool(tool: Tool) extends Command

final case class Visitor(windowInfo: WindowInfo, canvasObjectManager: CanvasObjectManager, commands: Option[Vector[Command]] = None, stopReason: Option[Vector[StopReason]] = None):
    def isStopped: Boolean = stopReason.isDefined
    def stopped(reason: StopReason): Visitor = copy(stopReason = Some(stopReason.getOrElse(Vector.empty) :+ reason))
    def commanded(command: Command): Visitor = copy(commands = Some(commands.getOrElse(Vector.empty) :+ command))
    def foreachCommand(f: Command => Unit): Unit = commands.foreach(_.foreach(f))
