package core

import java.awt.Component
import java.awt.Rectangle
import javax.swing.SwingUtilities

case class ComponentHandler(component: Component, bounds: Rectangle):
    component.setBounds(bounds)
    private var noneTranslatedBounds = bounds

    def updateCameraInfo(camera: Camera): Unit =
        val newBounds = camera.transform.createTransformedShape(bounds).getBounds()
        /*camera.transform.createTransformedShape()
        val newBounds = Rectangle(
            noneTranslatedBounds.x + translation.x,
            noneTranslatedBounds.y + translation.y,
            noneTranslatedBounds.width,
            noneTranslatedBounds.height
        )
        */
        if newBounds != component.getBounds() then
            SwingUtilities.invokeLater(() => component.setBounds(newBounds))
            //component.repaint()

    def getComponent: Component = component
        