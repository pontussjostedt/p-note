package customswingcomponents
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import javafx.embed.swing.JFXPanel
import java.io.File
import java.awt.Image
import javax.swing.JPanel
import javax.swing.JLabel

class PDFViewer(path: String="C:/Users/Pontu/OneDrive/Dokument/Funkfunktster_PONTUS_SJ%C3%96STEDT.pdf") extends JPanel:
    val label = JLabel()
    val document: PDDocument = PDDocument.load(File(path))
    val pdfRenderer: PDFRenderer = PDFRenderer(document)
    val page: PDPage = document.getPage(0)
    val image = pdfRenderer.renderImageWithDPI(0, 300)

    val scaledImage = image.getScaledInstance(800, 1200, Image.SCALE_SMOOTH)

    label.setIcon(new javax.swing.ImageIcon(scaledImage))
    add(label)

    //make sure the image is scaled to fit the size of the panel upon resize
    override def getPreferredSize() = new java.awt.Dimension(800, 1200)


    


