package core

import java.awt.Component
import java.awt.Rectangle
import javax.swing.SwingUtilities

case class ComponentHandler(component: Component, bounds: Rectangle):
    component.setBounds(bounds)
    private var noneTranslatedBounds = bounds

    def getBounds: Rectangle = noneTranslatedBounds


    def setBounds(bounds: Rectangle): Unit =
        //println("Setting bounds to " + bounds)
        noneTranslatedBounds = bounds

    def updateCameraInfo(camera: Camera): Unit =
        val newBounds = camera.transform.createTransformedShape(noneTranslatedBounds).getBounds()
        if newBounds != component.getBounds() then
            SwingUtilities.invokeLater(() => component.setBounds(newBounds))
            //component.repaint()

    def getComponent: Component = component
        