package core

import java.io.FileOutputStream
import java.io.File
import java.io.ObjectOutputStream
import scala.util.Try

sealed trait WriteIOResult
case object WriteSuccess extends WriteIOResult

trait ProjectSaver:
    def writeToDisk(canvasObjectManager: CanvasObjectManager, path: String): WriteIOResult
    def readFromDisk(path: String): Try[CanvasObjectManager]

    
class SimpleProjectSaver extends ProjectSaver:

    override def readFromDisk(path: String): Try[CanvasObjectManager] = 
        Try {
            val fileInputStream = java.io.FileInputStream(path)
            val objectInputStream = new java.io.ObjectInputStream(fileInputStream) {
                override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
                    try { Class.forName(desc.getName, false, getClass.getClassLoader) }
                    catch { case ex: ClassNotFoundException => super.resolveClass(desc) }
                }
            }

            val canvasObjectManager = objectInputStream.readObject().asInstanceOf[CanvasObjectManager]

            objectInputStream.close()
            fileInputStream.close()

            canvasObjectManager
        }

    override def writeToDisk(canvasObjectManager: CanvasObjectManager, path: String): WriteIOResult = 
        val fileOutputStream: FileOutputStream = FileOutputStream(File(path))
        val objectOutputStream: ObjectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(canvasObjectManager)
        objectOutputStream.close()
        fileOutputStream.close()
        WriteSuccess