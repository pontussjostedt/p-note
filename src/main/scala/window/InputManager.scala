package window

import java.awt.event.KeyListener
import java.awt.event.MouseListener
import java.awt.event.MouseEvent
import java.awt.event.KeyEvent
import scala.collection.mutable.Set
import core.*
import java.awt.RenderingHints.Key
import java.awt.geom.Rectangle2D
import javax.swing.JFrame
import java.awt.Component
import java.awt.event.MouseMotionListener
import java.awt.event.ComponentAdapter

case class KeyInfo(pressed: Boolean, justPressed: Boolean)
case class WindowInfo(keys: Map[Int, KeyInfo], leftMouseButton: KeyInfo, rightMouseButton: KeyInfo, windowMousePosition: Vector2, canvasMousePosition: Vector2, isActive: Boolean, parentSize: Vector2):
    def isKeyDown(keysDown: Int*): Boolean = keysDown.forall(keys.getOrElse(_, KeyInfo(false, false)).pressed)
    def inRectangle(rectangle: Rectangle2D): Boolean = rectangle.contains(canvasMousePosition)
    def debugString: String =
        s"""
        \u001b[2J \u001b[H
        keysDown: ${keys.mkString(", ")}
        mousePosition: $windowMousePosition
        leftMouse: ${leftMouseButton}
        rightMouse: ${rightMouseButton}
        isHovered: $isActive
        parentSize: $parentSize
        """





class InputManager extends KeyListener with MouseListener with MouseMotionListener {
    private val keysDown: Set[Int] = Set.empty
    private val justPressed: Set[Int] = Set.empty
    private var mousePos = Vector2.zero
    private var leftMouse: KeyInfo = KeyInfo(false, false)
    private var rightMouse: KeyInfo = KeyInfo(false, false)
    private var hoveringComponent: Boolean = false
    private var parentSize: Vector2 = Vector2.zero

    def getInputInfo(camera: Camera): WindowInfo =
        WindowInfo(
            keysDown.map(key => (key, KeyInfo(true, justPressed.contains(key)))).toMap,
            leftMouse,
            rightMouse,
            mousePos,
            camera.toCanvas(mousePos),
            hoveringComponent,
            parentSize,
        )
        
    def reset(): Unit = ???

    override def keyReleased(e: KeyEvent): Unit = 
        keysDown.remove(e.getKeyCode())   
    override def keyPressed(e: KeyEvent): Unit = 
        keysDown.add(e.getKeyCode())    

    override def keyTyped(e: KeyEvent): Unit = ()  

    override def mouseExited(e: MouseEvent): Unit = 
        hoveringComponent = false 
    
    override def mousePressed(e: MouseEvent): Unit = 
        e.getButton() match
            case MouseEvent.BUTTON1 => leftMouse = KeyInfo(true, true)
            case MouseEvent.BUTTON3 => rightMouse = KeyInfo(true, true)
            case _ => ()    
    override def mouseEntered(e: MouseEvent): Unit =
        hoveringComponent = true
    override def mouseClicked(e: MouseEvent): Unit = ()

    override def mouseReleased(e: MouseEvent): Unit = 
        e.getButton() match
            case MouseEvent.BUTTON1 => leftMouse = KeyInfo(false, false)
            case MouseEvent.BUTTON3 => rightMouse = KeyInfo(false, false)
            case _ => ()


    override def mouseMoved(e: MouseEvent): Unit = 
        mousePos = Vector2(e.getX(), e.getY())

    override def mouseDragged(e: MouseEvent): Unit = ()


    def attach(component: Component): Unit = {
        component.addKeyListener(this)
        component.addMouseListener(this)
        component.addMouseMotionListener(this)
        parentSize = Vector2(component.getWidth(), component.getHeight())
        component.addComponentListener(new ComponentAdapter {
        override def componentResized(e: java.awt.event.ComponentEvent): Unit = {
            val newDim = e.getComponent.getSize
            parentSize = Vector2(component.getWidth(), component.getHeight())
        }
    })
    }
  
}
