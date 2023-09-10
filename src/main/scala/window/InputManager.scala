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
import java.awt.Container
import javax.swing.SwingUtilities
import java.awt.Canvas


case class KeyInfo(pressed: Boolean, justPressed: Boolean)
case class WindowInfo(keys: Map[Int, KeyInfo], leftMouseButton: KeyInfo, rightMouseButton: KeyInfo, windowMousePosition: Vector2, canvasMousePosition: Vector2, isActive: Boolean, parentSize: Vector2, deltaTime: Double):
    def isKeyDown(keysDown: Int*): Boolean = keysDown.forall(keys.getOrElse(_, KeyInfo(false, false)).pressed)
    def isInputDown(inputs: (Int | MouseInput)*): Boolean = inputs.forall {
        case key: Int => keys.getOrElse(key, KeyInfo(false, false)).pressed
        case LeftMouse => leftMouseButton.pressed
        case RightMouse => rightMouseButton.pressed
    }
    def apply(inputs: (Int | MouseInput)*): Boolean = isInputDown(inputs: _*)
    def leftMouseDown: Boolean = leftMouseButton.pressed
    def rightMouseDown: Boolean = rightMouseButton.pressed
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
        dt: $deltaTime
        """
sealed trait MouseInput
case object LeftMouse extends MouseInput
case object RightMouse extends MouseInput






class InputManager(canvas: Canvas) extends KeyListener with MouseListener with MouseMotionListener {
    private val keysDown: Set[Int] = Set.empty
    private val justPressed: Set[Int] = Set.empty
    private var mousePos = Vector2.zero
    private var leftMouse: KeyInfo = KeyInfo(false, false)
    private var rightMouse: KeyInfo = KeyInfo(false, false)
    private var hoveringComponent: Boolean = false
    private var parentSize: Vector2 = Vector2.zero
    private var lastResetTime = System.nanoTime()

    def getInputInfo(camera: Camera): WindowInfo =
        WindowInfo(
            keysDown.map(key => (key, KeyInfo(true, justPressed.contains(key)))).toMap,
            leftMouse,
            rightMouse,
            mousePos,
            camera.toCanvas(mousePos),
            hoveringComponent,
            parentSize,
            (System.nanoTime() - lastResetTime) / 1e9
        )

        
    def reset(): Unit = 
        lastResetTime = System.nanoTime()
        justPressed.clear()

    def subscribeMouse(e: Component): Unit =
        e.addMouseListener(this)
        e.addMouseMotionListener(this)

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
        if e.getComponent() == canvas then
            hoveringComponent = true
    override def mouseClicked(e: MouseEvent): Unit = ()

    override def mouseReleased(e: MouseEvent): Unit = 
        e.getButton() match
            case MouseEvent.BUTTON1 => leftMouse = KeyInfo(false, false)
            case MouseEvent.BUTTON3 => rightMouse = KeyInfo(false, false)
            case _ => ()


    override def mouseMoved(e: MouseEvent): Unit = 
        val (x, y) = (e.getX(), e.getY())
        val newThing = SwingUtilities.convertPoint(e.getComponent(), x, y, canvas)
        mousePos = Vector2(newThing.x, newThing.y)

    override def mouseDragged(e: MouseEvent): Unit = 
        val (x, y) = (e.getX(), e.getY())
        val newThing = SwingUtilities.convertPoint(e.getComponent(), x, y, canvas)
        mousePos = Vector2(newThing.x, newThing.y)


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
