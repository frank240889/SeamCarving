import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*

fun findHorizontalSeamDijkstra(energyMap: Array<Array<Double>>): IntArray {
    val width = energyMap.size
    val height = energyMap[0].size

    // Creamos una matriz de distancias acumulativas
    val cumulativeDistances = Array(width) { DoubleArray(height) }
    for (x in 0 until width) {
        Arrays.fill(cumulativeDistances[x], Double.POSITIVE_INFINITY)
    }

    // Creamos una matriz de padres para almacenar los caminos
    val parents = Array(width) { IntArray(height) }

    // Inicializamos la primera columna de distancias acumulativas con los valores de la primera columna del mapa de energía
    for (y in 0 until height) {
        cumulativeDistances[0][y] = energyMap[0][y]
    }

    // Recorremos las columnas restantes y actualizamos las distancias acumulativas y los padres
    for (x in 1 until width) {
        for (y in 0 until height) {
            val currentEnergy = energyMap[x][y]
            val parentY = getMinParentY(cumulativeDistances, x, y)
            val parentDistance = cumulativeDistances[x - 1][parentY]
            val cumulativeDistance = parentDistance + currentEnergy
            if (cumulativeDistance < cumulativeDistances[x][y]) {
                cumulativeDistances[x][y] = cumulativeDistance
                parents[x][y] = parentY
            }
        }
    }

    var minY = 0
    for (y in 1 until height) {
        if (cumulativeDistances[width - 1][y] < cumulativeDistances[width - 1][minY]) {
            minY = y
        }
    }

    // Reconstruimos el Seam Carving vertical
    val seam = IntArray(width)
    seam[width - 1] = minY

    for (x in width - 2 downTo 0) {
        val prevY = parents[x + 1][seam[x + 1]]
        seam[x] = prevY
    }

    return seam
}

fun getMinParentY(cumulativeDistances: Array<DoubleArray>, x: Int, y: Int): Int {
    val parentYValues =  when (y) {
        0 -> {
            arrayOf(
                cumulativeDistances[x - 1][y],
                cumulativeDistances[x - 1][y + 1]
            ).toList()
        }
        cumulativeDistances.first().size - 1 -> {
            arrayOf(
                cumulativeDistances[x - 1][y - 1],
                cumulativeDistances[x - 1][y],
            ).toList()
        }
        else -> {
            arrayOf(
                cumulativeDistances[x - 1][y - 1],
                cumulativeDistances[x - 1][y],
                cumulativeDistances[x - 1][y + 1]
            ).toList()
        }
    }
    val allEqual = parentYValues.distinct().size == 1
    return if (allEqual) {
        y
    } else {
        val minDistance = parentYValues.minOrNull() ?: Double.POSITIVE_INFINITY
        if (y == 0) parentYValues.indexOfFirst { it == minDistance } else y - 1 + parentYValues.indexOfFirst { it == minDistance }
    }
}

fun findVerticalSeamDijkstra(energyMap: Array<Array<Double>>): IntArray {
    val width = energyMap.size
    val height = energyMap[0].size

    // Creamos una matriz de distancias acumulativas
    val cumulativeDistances = Array(height) { DoubleArray(width) }
    for (y in 0 until height) {
        Arrays.fill(cumulativeDistances[y], Double.POSITIVE_INFINITY)
    }

    // Creamos una matriz de padres para almacenar los caminos
    val parents = Array(height) { IntArray(width) }

    // Inicializamos la primera fila de distancias acumulativas con los valores de la primera fila del mapa de energía
    for (x in 0 until width) {
        cumulativeDistances[0][x] = energyMap[x][0]
    }

    // Recorremos las filas restantes y actualizamos las distancias acumulativas y los padres
    for (y in 1 until height) {
        for (x in 0 until width) {
            val currentEnergy = energyMap[x][y]
            val parentX = getMinParentX(cumulativeDistances, x, y)
            val parentDistance = cumulativeDistances[y - 1][parentX]
            val cumulativeDistance = parentDistance + currentEnergy
            if (cumulativeDistance < cumulativeDistances[y][x]) {
                cumulativeDistances[y][x] = cumulativeDistance
                parents[y][x] = parentX
            }
        }
    }

    // Encontramos el píxel con la menor distancia acumulativa en la última fila
    var minX = 0
    for (x in 1 until width) {
        if (cumulativeDistances[height - 1][x] < cumulativeDistances[height - 1][minX]) {
            minX = x
        }
    }

    // Reconstruimos el Seam Carving horizontal
    val seam = IntArray(height)
    seam[height - 1] = minX

    for (y in height - 2 downTo 0) {
        val prevX = parents[y + 1][seam[y + 1]]
        seam[y] = prevX
    }

    return seam
}

fun getMinParentX(cumulativeDistances: Array<DoubleArray>, x: Int, y: Int): Int {
    val parentXValues =  when (x) {
        0 -> {
            arrayOf(
                cumulativeDistances[y - 1][x],
                cumulativeDistances[y - 1][x + 1]
            ).toList()
        }
        cumulativeDistances.first().size - 1 -> {
            arrayOf(
                cumulativeDistances[y - 1][x - 1],
                cumulativeDistances[y - 1][x]
            ).toList()
        }
        else -> {
            arrayOf(
                cumulativeDistances[y - 1][x - 1],
                cumulativeDistances[y - 1][x],
                cumulativeDistances[y - 1][x + 1]
            ).toList()
        }
    }
    val allEqual = parentXValues.distinct().size == 1
    return if (allEqual) {
        x
    } else {
        val minDistance = parentXValues.minOrNull() ?: Double.POSITIVE_INFINITY
        if (x == 0) parentXValues.indexOfFirst { it == minDistance } else x - 1 + parentXValues.indexOfFirst { it == minDistance }
    }
}

fun highlightVerticalSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage
): BufferedImage {
    val width = energyMap.size
    val height = energyMap[0].size

    // Creamos un nuevo BufferedImage para almacenar el resultado
    val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    // Copiamos los píxeles de la imagen original al resultado
    for (x in 0 until width) {
        for (y in 0 until height) {
            val rgb = originalImage.getRGB(x, y)
            resultImage.setRGB(x, y, rgb)
        }
    }

    // Encontramos el Seam Carving horizontal de menor energía
    val seam = findVerticalSeamDijkstra(energyMap)
    println(seam.contentToString())
    // Pintamos los píxeles del Seam Carving horizontal en rojo en el resultado
    for (y in 0 until height) {
        val x = seam[y]
        resultImage.setRGB(x, y, Color.RED.rgb)
    }

    return resultImage
}

fun highlightHorizontalSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage
): BufferedImage {
    val width = energyMap.size
    val height = energyMap[0].size

    // Creamos un nuevo BufferedImage para almacenar el resultado
    val resultImage = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

    // Copiamos los píxeles de la imagen original al resultado
    for (x in 0 until width) {
        for (y in 0 until height) {
            val rgb = originalImage.getRGB(x, y)
            resultImage.setRGB(x, y, rgb)
        }
    }

    // Encontramos el Seam Carving vertical de menor energía
    val seam = findHorizontalSeamDijkstra(energyMap)

    // Pintamos los píxeles del Seam Carving vertical en rojo en el resultado
    for (x in 0 until width) {
        val y = seam[x]
        resultImage.setRGB(x, y, Color.RED.rgb)
    }

    return resultImage
}

fun removeVerticalSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage
): BufferedImage {
    val width = energyMap.size
    val height = energyMap[0].size

    val resultImage = BufferedImage(width - 1, height, BufferedImage.TYPE_INT_RGB)
    val seam = findVerticalSeamDijkstra(energyMap)
    for (y in 0 until height) {
        var destX = 0
        for (srcX in 0 until width) {
            if (srcX != seam[y]) {
                val rgb = originalImage.getRGB(srcX, y)
                resultImage.setRGB(destX, y, rgb)
                destX++
            }
        }
    }
    return resultImage
}

fun removeHorizontalSeamDijkstra(
    energyMap: Array<Array<Double>>,
    originalImage: BufferedImage
): BufferedImage {
    val width = energyMap.size
    val height = energyMap[0].size

    // Creamos un nuevo BufferedImage para almacenar el resultado
    val resultImage = BufferedImage(width, height - 1, BufferedImage.TYPE_INT_RGB)

    // Encontramos el Seam Carving vertical de menor energía
    val seam = findHorizontalSeamDijkstra(energyMap)

    // Copiamos los píxeles de la imagen original al resultado
    for (x in 0 until width) {
        var destY = 0
        for (y in 0 until height) {
            if (y != seam[x]) {
                val rgb = originalImage.getRGB(x, y)
                resultImage.setRGB(x, destY, rgb)
                destY++
            }
        }
    }

    return resultImage
}