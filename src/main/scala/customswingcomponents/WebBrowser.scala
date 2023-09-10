package customswingcomponents

import java.awt.Component
import javafx.embed.swing.JFXPanel
import javafx.application.Platform

class WebBrowser(initialURL: String) extends JFXPanel:
    Platform.runLater {() => 
        val webView = javafx.scene.web.WebView()
        webView.getEngine().load(initialURL)
        val scene = javafx.scene.Scene(webView)
        setScene(scene)
    }