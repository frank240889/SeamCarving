import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.sqrt

fun main(args: Array<String>) {
    //val inputFile = "/Users/franco/IdeaProjects/Seam Carving/Seam Carving/task/test/small.png"
    //val outputFile = "/Users/franco/IdeaProjects/Seam Carving/Seam Carving/task/test/small-seam.png"
    val inputFile = getCommandLineArgument(args, "-in")
    val outputFile = getCommandLineArgument(args, "-out")
    val verticalSeamsToRemove = getCommandLineArgument(args, "-width").toInt()
    val horizontalSeamsToRemove = getCommandLineArgument(args, "-height").toInt()
    var inputImage = loadImage(inputFile)
    var ouputImage = inputImage
    for (vSeam in 0 until verticalSeamsToRemove) {
        val energyMatrix = calculateEnergy(inputImage)
        ouputImage = removeSeamDijkstra(energyMatrix, inputImage, Seam.VERTICAL)
        inputImage = ouputImage
    }

    for (hSeam in 0 until horizontalSeamsToRemove) {
        val energyMatrix = calculateEnergy(inputImage)
        ouputImage = removeSeamDijkstra(energyMatrix, inputImage, Seam.HORIZONTAL)
        inputImage = ouputImage
    }
    saveImage(ouputImage, outputFile)
    //val maxEnergyValue = findMaxEnergyValue(energyImage)
    //val normalizedEnergyImage = normalizeEnergizedImage(energyImage, maxEnergyValue)
    //val verticalSeamImage = highlightSeamDijkstra(energyImage, inputImage, Seam.HORIZONTAL)
}

fun loadImage(filePath: String): BufferedImage {
    return ImageIO.read(File(filePath))
}

fun calculateEnergy(image: BufferedImage): Array<Array<Double>> {
    val width = image.width
    val height = image.height
    val energyMatrix = Array(width) { Array(height) { 0.0 } }

    for (w in 0 until width) {
        for (h in 0 until height) {
            val x = when(w) {
                0 -> 1 // Is first? Then right shift by 1
                width - 1 -> width - 2 // Is last? Then left shift by 1
                else -> w
            }
            val xDelta = getDelta(image, x - 1, h, x + 1, h)

            val y = when(h) {
                0 -> 1 // Is first? Then right shift by 1
                height - 1 -> height - 2
                else -> h
            }
            val yDelta = getDelta(image, w, y - 1, w, y + 1)
            val energy = sqrt(xDelta + yDelta)
            energyMatrix[w][h] = energy
        }
    }
    return energyMatrix
}

fun getDelta(image: BufferedImage, x1: Int, y1: Int, x2: Int, y2: Int): Double {
    val leftPixel = getColor(image, x1, y1)
    val rightPixel = getColor(image, x2, y2)
    val redDelta = abs((leftPixel.red - rightPixel.red))
    val greenDelta = abs((leftPixel.green - rightPixel.green))
    val blueDelta = abs((leftPixel.blue - rightPixel.blue))
    return ((redDelta * redDelta) + (greenDelta * greenDelta) + (blueDelta * blueDelta)).toDouble()
}

fun getColor(image: BufferedImage, x: Int, y: Int): Color {
    return Color(image.getRGB(x, y))
}

fun findMaxEnergyValue(image: Array<Array<Double>>): Double {
    var maxEnergyValue = 0.0

    val width = image.size
    val height = image.first().size

    for (x in 0 until width) {
        for (y in 0 until height) {
            maxEnergyValue = maxOf(maxEnergyValue, image[x][y])
        }
    }
    return maxEnergyValue
}

fun normalizeEnergizedImage(image: Array<Array<Double>>, maxEnergyValue: Double): BufferedImage {
    val width = image.size
    val height = image.first().size

    val normalizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    for (x in 0 until width) {
        for (y in 0 until height) {
            val energyValue = image[x][y]
            val intensity = (255 * energyValue / maxEnergyValue).toInt().coerceIn(0..255)
            normalizedImage.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
        }
    }

    return normalizedImage
}

fun saveImage(image: BufferedImage, filePath: String) {
    ImageIO.write(image, "png", File(filePath))
}

fun getCommandLineArgument(args: Array<String>, argumentName: String): String {
    val index = args.indexOf(argumentName)
    if (index > -1 && index + 1 < args.size) {
        return args[index + 1]
    }
    throw IllegalArgumentException("Missing command-line argument: $argumentName")
}

fun highlightSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage,
    horizontal: Seam): BufferedImage {
    return if (horizontal == Seam.VERTICAL) {
        highlightVerticalSeamDijkstra(energyMap, originalImage)
    } else {
        highlightHorizontalSeamDijkstra(energyMap, originalImage)
    }
}

fun removeSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage,
    orientation: Seam): BufferedImage {
    return if (orientation == Seam.VERTICAL) {
        removeVerticalSeamDijkstra(energyMap, originalImage)
    } else {
        removeHorizontalSeamDijkstra(energyMap, originalImage)
    }
}

enum class Seam {
    VERTICAL,
    HORIZONTAL
}
