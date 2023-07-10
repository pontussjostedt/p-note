package core
import scala.util.control.Breaks.*
import scala.collection.mutable.ArrayBuffer
case class Segment(point: Vector2, var handleIn: Option[Vector2], var handleOut: Option[Vector2])
case class ErrorType(error: Double, index: Int)
def pointNormalize(p: Vector2, length: Double = 1D): Vector2 =
    if p.mag == 0 then Vector2(0, 0)
    else
    p * length / p.mag

def pointLength(p: Vector2): Double =
    math.hypot(p.x, p.y)
extension (d: Double | Int)
    def format: String = d match
        case d: Double => 
            val p = (math.round(d * math.pow(10, 7))/math.pow(10, 7).toDouble).toString()
            var out = if p.contains(".") then 
                p.reverse
                    .dropWhile(_ == '0')
                    .dropWhile(_ == '.')
                    .reverse
            else p
            out = (if out == "-0" then "0" else out)
            if out.contains("E-") then
                val split = out.split("E-")
                val num = split.head
                val exp = split.last.toInt
                val numNoDot = num.replace(".", "").reverse.dropWhile(_ == '0').reverse
                out = s"0.${"0" * (exp - 1)}${numNoDot}"
            out
        case i: Int => i.toString

object PathSimplification {
    given Int = 0
    given Double = 0.0
    val EPSILON = 1e-12
    val machine_epsilon = 1.12e-16
    def isMachineZero(x: Double): Boolean = x >= -machine_epsilon && x <= machine_epsilon

    def fit(points: ArrayBuffer[Vector2], closed: Boolean, error: Double): ArrayBuffer[Segment] = 

        //println("\n\n**** STARTING **** \n\n")
        if points.length <= 1 then return ArrayBuffer()
        if closed then ()

        val length = points.length

        val segments = ArrayBuffer(Segment(points.head, None, None))
        fitCubic(points, segments, error, 0, length - 1, points(1) - points(0), points(length - 2) - points(length - 1))
        //println(s"[${points.map(formatPoint).mkString(", ")}]")
        println(s"Before: ${points.length} After: ${segments.length}")
        segments

    def fitCubic(points: ArrayBuffer[Vector2], segments: ArrayBuffer[Segment], error: Double, first: Int, last: Int, tan1: Vector2, tan2: Vector2): Unit =
        //println("**** FITCUBIC ****")
        //println(s"first: ${first} last: ${last} error: ${error.format}\ntan1: ${formatPoint(tan1)}  tan2: ${formatPoint(tan2)}")
        if last - first == 1 then
            //println("\t**** LAST - FIRST BRANCH ****")
            val pt1 = points(first)
            val pt2 = points(last)
            val dist = pt1.distance(pt2) / 3 //magic number?!??!
            addCurve(segments, ArrayBuffer(pt1, pt1 + tan1.normalized * dist, pt2 + tan2.normalized * dist, pt2))
        else
            val uPrime = chordLengthParameterize(points, first, last)
            var maxError = math.max(error, error * error)
            //println(s"\tmaxErrorSet: ${maxError.format}")
            var split: Int = 0
            var parametersInOrder = true

            breakable {
                for i <- 0 to 4 do
                    val curve = generateBezier(points, first, last, uPrime, tan1, tan2)
                    var max = findMaxError(points, first, last, curve, uPrime)
    
                    if max.error < error && parametersInOrder then
                        //println("\t**** return path ****")
                        addCurve(segments, curve)
                        return
                    split = max.index
                    //println(s"split: ${split}, maxError: ${maxError.format}, max.error: ${max.error.format}")
                    if max.error >= maxError then break()
                    parametersInOrder = reparameterize(points, first, last, uPrime, curve)
                    maxError = max.error
                    //println(s"\tmaxError updated: ${maxError.format}")
            }
            //println("\t**** TANCENTRE ****")
            val tanCenter = points(split - 1) - points(split + 1)
            fitCubic(points, segments, error, first, split, tan1, tanCenter)
            fitCubic(points, segments, error, split, last, tanCenter * -1, tan2)


        

    def addCurve(segments: ArrayBuffer[Segment], curve: ArrayBuffer[Vector2]) =
        //println("**** ADDCURVE ****")
        //println(s"curve: [${curve.map(formatPoint).mkString(" ")}]")
        val prev = segments.last
        prev.handleOut = Some(curve(1) - curve(0))
        //println(s"prev.handleOut: ${formatPoint(prev.handleOut.get)}")
        segments += Segment(curve(3), Some(curve(2) - curve(3)), None)

    def formatPoint(p: Vector2): String = 
        if p == null then "null"
        else
        s"Point(${p.x.format}, ${p.y.format})"
    def generateBezier(points: ArrayBuffer[Vector2], first: Int, last: Int, uPrime: ArrayBuffer[Double], tan1: Vector2, tan2: Vector2) =
        //println("**** GENERATEBEZIER ****")
        //println(s"first: ${first} last: ${last} \ntan1: ${formatPoint(tan1)}  tan2: ${formatPoint(tan2)} \nuPrime: [${uPrime.map(_.format).mkString(" ")}]")
        //println(s"pointslength: ${points.length}, points: [${points.map(formatPoint).mkString(" ")}]")
        val epsilon = EPSILON
        val pt1 = points(first)
        val pt2 = points(last)
        //println(s"pt1: ${formatPoint(pt1)}, pt2: ${formatPoint(pt2)}")

        val C = ArrayBuffer(ArrayBuffer[Double](0, 0), ArrayBuffer[Double](0, 0))
        val X = ArrayBuffer[Double](0, 0)

        val len = last - first + 1
        for i <- 0 until len do
            //println("\t**** BEZIER LOOP ****")
            val u = uPrime(i)
            val t = 1 - u
            val b = 3 * u * t
            val b0 = t * t * t
            val b1 = b * t
            val b2 = b * u
            //println(s"b3 in: u: ${u.format}")
            val b3 = u * u * u
            //println(s"a1 in: tan1: ${formatPoint(tan1)}, b1: ${b1.format}")
            val a1 = pointNormalize(tan1, b1)
            //println(s"a1 out: ${formatPoint(a1)}")
            //println(s"a2 in: tan2: ${formatPoint(tan2)}, b2: ${b2.format}")
            val a2 = pointNormalize(tan2, b2)
            //println(s"a2 out: ${formatPoint(a2)}")
            //println(s"i: ${i}, u: ${u.format}, t: ${t.format}, a1: ${formatPoint(a1)}, a2: ${formatPoint(a2)}, b: ${b.format}, b0: ${b0.format}, b1: ${b1.format}, b2: ${b2.format}, b3: ${b3.format}")
            val tmp = points(first + i) - (pt1 * (b0 + b1)) - (pt2 * (b2+b3))
            //println(s"tmp = ${formatPoint(points(first + i))} - ${formatPoint(pt1)} * ${(b0 + b1).format} - ${formatPoint(pt2)} * ${(b2 + b3).format} = ${formatPoint(tmp)}")
            //println(s"out tmp: ${formatPoint(tmp)}")
            C(0)(0) += a1.dot(a1)
            C(0)(1) += a1.dot(a2)
            
            C(1)(0) = C(0)(1)
            C(1)(1) += a2.dot(a2)
            //val tmp2 = Vector2(t * b1, u * b2)
            X(0) += a1.dot(tmp)
            X(1) += a2.dot(tmp)
        //println(s"C: [${C.map(_.map(_.format).mkString(" ")).mkString("; ")}]")
        //println(s"X: [${X.map(format).mkString(" ")}]")
        val detC0C1 = C(0)(0) * C(1)(1) - C(1)(0) * C(0)(1)
        var alpha1 = 0D
        var alpha2 = 0D
        if math.abs(detC0C1) > epsilon then
            //println("\t**** Kramers Rule ****")
            val detC0X = C(0)(0) * X(1)    - C(1)(0) * X(0)
            val detXC1 = X(0)    * C(1)(1) - X(1)    * C(0)(1)

            alpha1 = detXC1 / detC0C1
            alpha2 = detC0X / detC0C1
            //println(s"detC0C1: ${detC0C1.format} detC0X: ${detC0X.format} detXC1: ${detXC1.format}")
            //println(s"alpha1: ${alpha1.format} alpha2: ${alpha2.format}")
        else
            val c0 = C(0)(0) + C(0)(1)
            val c1 = C(1)(0) + C(1)(1)
            val alphaVal = if math.abs(c0) > epsilon then X(0) / c0 else if math.abs(c1) > epsilon then X(1) / c1 else 0
            alpha1 = alphaVal
            alpha2 = alphaVal
            //println("\t**** Not Kramer ****")
            //println(s"alpha1: ${alpha1.format} alpha2: ${alpha2.format}")

        val segLength = pt2.distance(pt1)
        val eps = epsilon * segLength
        //println(s"segLength: ${segLength.format}, eps: ${eps.format}")
        var handle1: Vector2 = null
        var handle2: Vector2 = null

        if alpha1 < eps || alpha2 < eps then
            alpha1 = segLength / 3D
            alpha2 = segLength / 3D
            //println("\t**** Wu/Barsky ****")
            //println(s"alpha1: ${alpha1.format} alpha2: ${alpha2.format}")
        else
            val line = pt2 - pt1
            handle1 = tan1.normalized * alpha1
            handle2 = tan2.normalized * alpha2
            //println("\t**** ELSE Wu/Barsky ****")
            //println(s"handle1: ${formatPoint(handle1)} handle2: ${formatPoint(handle2)} line: ${formatPoint(line)}")

            if handle1.dot(line) - handle2.dot(line) > segLength * segLength then
                alpha1 = segLength / 3D
                alpha2 = segLength / 3D
                handle1 = null
                handle2 = null
                //println("\t\t**** ELSE Wu/Barsky 2 ****")
                //println(s"alpha1: ${alpha1.format} alpha2: ${alpha2.format}")
        //println(s"handle1: ${formatPoint(handle1)} handle2: ${formatPoint(handle2)}")
        //println(s"alpha1: ${alpha1.format} alpha2: ${alpha2.format}")
        //println(s"pt1: ${formatPoint(pt1)} pt2: ${formatPoint(pt2)}")
        
        val out = ArrayBuffer(
            pt1,
            pt1 + (if handle1 == null then tan1.normalized * alpha1 else handle1), 
            pt2 + (if handle2 == null then tan2.normalized * alpha2 else handle2), 
            pt2
        )
        //println(s"bezier out: [${out.map(formatPoint).mkString(" ")}]")
        out





    def chordLengthParameterize(points: ArrayBuffer[Vector2], first: Int, last: Int): ArrayBuffer[Double] =
        //println("**** chordLengthParameterize ****")
        //println(s"first: ${first}, last: ${last}")
        val u = ArrayBuffer[Double]()
        u += 0.0
        for i <- first + 1 to last do
            u.jsAssign(i - first, u(i - first - 1) + points(i).distance(points(i - 1)))
            //u(i - first) = u(i - first - 1) + points(i).distance(points(i - 1)) //
        
        val m = last - first
        for i <- 1 to m do
            u(i) /= u(m)

        //println(s"\nout: [${u.map(_.format).mkString(" ")}]")
        u

    def findMaxError(points: ArrayBuffer[Vector2], first: Int, last: Int, curve: ArrayBuffer[Vector2], u: ArrayBuffer[Double]): ErrorType =
        //println("**** findMaxError ****")
        //println(s"first: ${first}, last: ${last} \ncurve: [${curve.map(formatPoint).mkString(" ")}] \nu: [${u.map(_.format).mkString(" ")}]")
        var index: Int = (last - first + 1) / 2
        var maxDist: Double = 0
        for i <- first + 1 until last do
            val P = evaluate(3, curve, u(i - first))
            val v = P - points(i)
            val dist = v.x * v.x + v.y * v.y
            if dist >= maxDist then
                //println("\t****IF MAXERROR****")
                maxDist = dist
                index = i
        //println(s"findMaxError out: maxDist: ${maxDist.format}, index: ${index}")
        ErrorType(maxDist, index)

    def evaluate(degree: Int, curve: ArrayBuffer[Vector2], t: Double): Vector2 =
        //println("**** evaluate ****")
        //println(s"degree: ${degree.format}, t: ${t.format}")
        //println(s"curve: [${curve.map(formatPoint).mkString(" ")}]")
        val tmp = curve.clone()
        for i <- 1 to degree do
            for j <- 0 to degree - i do
                tmp(j) = tmp(j) * (1 - t) + tmp(j + 1) * t
        val out = tmp.head
        //println(s"evaluate out: ${formatPoint(out)}")
        out

    def reparameterize(points: ArrayBuffer[Vector2], first: Int, last: Int, u: ArrayBuffer[Double], curve: ArrayBuffer[Vector2]): Boolean =
        //println("**** REPARAMETERIZE ****")
        //println(s"first: ${first} last: ${last} \ncurve: [${curve.map(formatPoint).mkString(" ")}] \nu: [${u.map(_.format).mkString(" ")}]")
        var out = true
        for i <- first to last do
            u.jsAssign(i - first, findRoot(curve, points(i), u(i - first))) //findRoot(curve, points(i), u(i - first))
        breakable {
            for i <- 1 until u.length do
                if u(i) <= u(i - 1) then
                    out = false
        }
        //println(s"out: ${out}")
        out


    def findRoot(curve: ArrayBuffer[Vector2], point: Vector2, u: Double): Double =
        //println("**** FIND ROOT ****")
        //println(s"point: ${formatPoint(point)} u: ${u.format}")
        //println(s"curve: [${curve.map(formatPoint).mkString(" ")}]")
        val curve1 = ArrayBuffer[Vector2]()
        val curve2 = ArrayBuffer[Vector2]()

        for i <- 0 to 2 do
            curve1 += (curve(i + 1) - curve(i)) * 3 //Mby js assign

        for i <- 0 to 1 do
            curve2 += (curve1(i + 1) - curve1(i)) * 2 //Mby js assign

        val pt = evaluate(3, curve, u)
        val pt1 = evaluate(2, curve1, u)
        val pt2 = evaluate(1, curve2, u)
        val diff = pt - point
        val df = pt1.dot(pt1) + diff.dot(pt2)

        val out = if isMachineZero(df) then u else u - diff.dot(pt1) / df
        //println(s"findRoot out: ${out.format}")
        out


    extension [A](buffer: ArrayBuffer[A])
        infix def jsAssign(index: Int, value: A)(using default: A) =
            if index >= buffer.length then
                buffer ++= ArrayBuffer.fill(index - buffer.length + 1)(default)
            buffer(index) = value
}
