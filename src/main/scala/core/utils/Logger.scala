package core

object Logger {
    private var log = Vector[String]()
    def log(message: String): Unit = {println(message); log :+= message}
}
