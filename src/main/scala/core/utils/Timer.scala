package core


final case class Timer(msDelay: Long, private val startOnCd: Boolean = true):
    var lastProc = if startOnCd then System.currentTimeMillis() else -msDelay
    def isOver: Boolean = lastProc + msDelay <= System.currentTimeMillis()
    def isOverReset: Boolean =
        val out = lastProc + msDelay <= System.currentTimeMillis()
        if out then lastProc = System.currentTimeMillis()
        out


case class FPSMeassure(collectionPeriod: Long):
    val timer = Timer(collectionPeriod)
    var frameCount = 0
    def tick(): Unit = 
        frameCount += 1
        if timer.isOverReset then
            println(s"FPS = ${1000*frameCount/collectionPeriod.toDouble}")
            frameCount = 0



class CodeClocker(name: String) extends Serializable:
    def offer(block: () => Unit): Long = 
        val start = System.nanoTime()
        block()
        val end = System.nanoTime()
        end - start

    def offerThenPassThrough[A](block: () => A): A = 
        val start = System.nanoTime()
        val out = block()
        val end = System.nanoTime()
        println(s"$name: Time taken: ${CodeClocker.nanoToMs(end - start)}ms")
        out

object CodeClocker:
    def nanoToMs(nano: Long): Double = nano * 1e-6D

class MultiClocker:
    def offer(n: Int)(block: () => Unit): Long = 
        val start = System.nanoTime()
        for i <- 0 until n do block()
        val end = System.nanoTime()
        end - start



