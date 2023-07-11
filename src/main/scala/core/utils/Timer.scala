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



class CodeClocker(timer: Timer, minSammpleSize: Int):
    var times = Vector.empty[Long]
    def offer(block: () => Unit): Unit = 
        val start = System.nanoTime()
        block()
        val end = System.nanoTime()
        times :+= end - start
        if timer.isOverReset && times.length > minSammpleSize then
            //println(times.reverse.take(minSammpleSize).mkString(","))
            val avgTime = times.reverse.take(minSammpleSize).sum/(minSammpleSize * 1e6D)
            println(s"Code block took ${avgTime}ms to execute           (fps: ${1000/avgTime})")

class MultiClocker:
    def offer(n: Int)(block: () => Unit): Long = 
        val start = System.nanoTime()
        for i <- 0 until n do block()
        val end = System.nanoTime()
        end - start



