package core

import java.awt.Graphics2D
import java.awt.{Shape}
import scala.collection.generic.{Growable, Shrinkable}
 import scala.collection.mutable.Shrinkable
import java.awt.geom.Rectangle2D

trait GeometryStore[A] extends Growable[A], Shrinkable[A]:
    def queryContains(selector: Shape): Seq[A]
    def draw(g2d: Graphics2D): Unit
    def queryClippingRect(viewArea: Rectangle2D): Seq[A]
    def queryClippingShape(selector: Shape): Seq[A]
    def getAll: Seq[A]

