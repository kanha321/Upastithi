package com.kanhaji.upasthiti.data

import android.util.Log

/**
 * Represents a single cell in the resolved grid.
 * A cell may span multiple rows and/or columns (merged cells).
 *
 * @param row Row index (0-based, where row 0 is the header).
 * @param col Column index (0-based, where col 0 is the day-label column).
 * @param rowSpan Number of rows this cell spans (≥ 1).
 * @param colSpan Number of columns this cell spans (≥ 1).
 * @param left Left X boundary in page coordinates.
 * @param right Right X boundary in page coordinates.
 * @param top Top Y boundary in page coordinates.
 * @param bottom Bottom Y boundary in page coordinates.
 */
data class GridCell(
    val row: Int,
    val col: Int,
    val rowSpan: Int = 1,
    val colSpan: Int = 1,
    val left: Float,
    val right: Float,
    val top: Float,
    val bottom: Float
) {
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f
    val width: Float get() = right - left
    val height: Float get() = bottom - top
}

/**
 * The resolved grid structure: ordered row/column edges and
 * the set of cells (with merge spans) that compose the table.
 */
data class GridStructure(
    /** Sorted unique Y-coordinates for row boundaries (top of each row). */
    val rowEdges: List<Float>,
    /** Sorted unique X-coordinates for column boundaries (left of each col). */
    val colEdges: List<Float>,
    /** All cells in the grid, including merged cells. Keyed by (row, col) of top-left corner. */
    val cells: Map<Pair<Int, Int>, GridCell>,
    /** The timetable grid region bounds. */
    val tableLeft: Float,
    val tableRight: Float,
    val tableTop: Float,
    val tableBottom: Float
)

/**
 * Builds a `GridStructure` from extracted vector lines.
 *
 * Algorithm:
 * 1. Cluster all horizontal line Y-coordinates into distinct row edges.
 * 2. Cluster all vertical line X-coordinates into distinct column edges.
 * 3. For each cell `(row, col)`, check whether interior dividers exist.
 *    - If a vertical divider between col `j` and `j+1` is **missing** within
 *      the row's Y-range, the cell is merged rightward.
 *    - If a horizontal divider between row `i` and `i+1` is **missing** within
 *      the col's X-range, the cell is merged downward.
 */
object GridCellResolver {

    private const val TAG = "GridCellResolver"

    /**
     * Tolerance for clustering nearby coordinate values into a single edge.
     * Two line coordinates within this distance are considered the same edge.
     */
    private const val EDGE_CLUSTER_TOLERANCE = 3.0f

    /**
     * Build a grid from the extracted lines.
     *
     * @param lines The horizontal and vertical line segments from [PdfLineExtractor].
     * @param tableRegionMaxY Optional Y-coordinate limit. Lines below this are
     *        considered part of the legend/footer, not the timetable grid.
     *        If null, all lines are included.
     * @return A [GridStructure] if a valid grid was found, or null if insufficient lines.
     */
    fun buildGrid(lines: PdfGridLines, tableRegionMaxY: Float? = null): GridStructure? {
        // Filter lines to only the timetable grid region if a boundary is specified
        val hLines = if (tableRegionMaxY != null) {
            lines.horizontal.filter { it.hY <= tableRegionMaxY + EDGE_CLUSTER_TOLERANCE }
        } else lines.horizontal
        val vLines = if (tableRegionMaxY != null) {
            lines.vertical.filter { Math.min(it.y0, it.y1) <= tableRegionMaxY + EDGE_CLUSTER_TOLERANCE }
        } else lines.vertical

        if (hLines.size < 3 || vLines.size < 3) {
            Log.w(TAG, "buildGrid: Too few lines (${hLines.size} H, ${vLines.size} V) — cannot build grid")
            return null
        }

        // Step 1: Cluster horizontal line Y-values into distinct row edges
        val rawRowEdges = clusterValues(hLines.map { it.hY })
        // Step 2: Cluster vertical line X-values into distinct column edges
        val rawColEdges = clusterValues(vLines.map { it.vX })

        // Step 2b: Remove degenerate narrow columns/rows (border artifacts).
        // Some PDFs have close-but-separate vertical lines (e.g., right edge of
        // left table section at x=469.5 and left edge of right section at x=473.5)
        // that create 4pt-wide phantom columns. Filter these out.
        val colEdges = filterDegenerateEdges(rawColEdges, minSpan = 10f)
        val rowEdges = filterDegenerateEdges(rawRowEdges, minSpan = 5f)

        if (rowEdges.size < 3 || colEdges.size < 3) {
            Log.w(TAG, "buildGrid: Too few edges (${rowEdges.size} rows, ${colEdges.size} cols)")
            return null
        }

        Log.d(TAG, "buildGrid: ${rowEdges.size} row edges: ${rowEdges.map { "%.1f".format(it) }}")
        Log.d(TAG, "buildGrid: ${colEdges.size} col edges: ${colEdges.map { "%.1f".format(it) }}")

        val tableTop = rowEdges.first()
        val tableBottom = rowEdges.last()
        val tableLeft = colEdges.first()
        val tableRight = colEdges.last()

        // Step 3: Build cells with merge detection
        val numRows = rowEdges.size - 1
        val numCols = colEdges.size - 1

        // Pre-index: for each row band, which vertical dividers exist?
        // For each col band, which horizontal dividers exist?
        // A vertical divider at column edge `colEdges[j]` exists within row band `i`
        // if there is a vertical line segment at x ≈ colEdges[j] that spans through
        // the Y-range [rowEdges[i], rowEdges[i+1]].
        val cells = mutableMapOf<Pair<Int, Int>, GridCell>()
        val consumed = mutableSetOf<Pair<Int, Int>>() // cells consumed by a merge

        for (r in 0 until numRows) {
            for (c in 0 until numCols) {
                if (consumed.contains(r to c)) continue

                // Determine column span: look rightward
                var colSpan = 1
                for (nextC in c + 1 until numCols) {
                    val dividerX = colEdges[nextC]
                    val rowTop = rowEdges[r]
                    val rowBottom = rowEdges[r + 1]
                    if (hasVerticalDivider(vLines, dividerX, rowTop, rowBottom)) {
                        break
                    }
                    colSpan++
                }

                // Determine row span: look downward
                var rowSpan = 1
                for (nextR in r + 1 until numRows) {
                    val dividerY = rowEdges[nextR]
                    val colLeft = colEdges[c]
                    val colRight = colEdges[c + colSpan] // account for colspan
                    if (hasHorizontalDivider(hLines, dividerY, colLeft, colRight)) {
                        break
                    }
                    rowSpan++
                }

                val cell = GridCell(
                    row = r,
                    col = c,
                    rowSpan = rowSpan,
                    colSpan = colSpan,
                    left = colEdges[c],
                    right = colEdges[c + colSpan],
                    top = rowEdges[r],
                    bottom = rowEdges[r + rowSpan]
                )
                cells[r to c] = cell

                // Mark all covered positions as consumed
                for (dr in 0 until rowSpan) {
                    for (dc in 0 until colSpan) {
                        if (dr != 0 || dc != 0) {
                            consumed.add((r + dr) to (c + dc))
                        }
                    }
                }
            }
        }

        Log.d(TAG, "buildGrid: Built ${cells.size} cells (${consumed.size} consumed by merges)")

        // Log merged cells specifically
        cells.values.filter { it.colSpan > 1 || it.rowSpan > 1 }.forEach { cell ->
            Log.d(TAG, "buildGrid: MERGED cell (${cell.row},${cell.col}) span=${cell.colSpan}x${cell.rowSpan} " +
                    "bounds=[${cell.left},${cell.top}]->[${cell.right},${cell.bottom}]")
        }

        return GridStructure(
            rowEdges = rowEdges,
            colEdges = colEdges,
            cells = cells,
            tableLeft = tableLeft,
            tableRight = tableRight,
            tableTop = tableTop,
            tableBottom = tableBottom
        )
    }

    /**
     * Assign text blocks to grid cells based on containment:
     * A block belongs to the cell whose bounds contain the block's center point.
     */
    fun assignTextToGrid(
        grid: GridStructure,
        blocks: List<TextBlock>
    ): Map<Pair<Int, Int>, MutableList<TextBlock>> {
        val assignment = mutableMapOf<Pair<Int, Int>, MutableList<TextBlock>>()

        for (block in blocks) {
            val cx = block.centerX
            val cy = block.centerY

            // Skip blocks outside the table region
            if (cx < grid.tableLeft - 2f || cx > grid.tableRight + 2f ||
                cy < grid.tableTop - 2f || cy > grid.tableBottom + 2f) {
                continue
            }

            // Find which cell contains this block's center
            var assigned = false
            for ((key, cell) in grid.cells) {
                if (cx >= cell.left - 1f && cx <= cell.right + 1f &&
                    cy >= cell.top - 1f && cy <= cell.bottom + 1f) {
                    assignment.getOrPut(key) { mutableListOf() }.add(block)
                    assigned = true
                    break
                }
            }

            if (!assigned) {
                Log.v(TAG, "assignTextToGrid: Unassigned block '${block.text}' at (${cx}, ${cy})")
            }
        }

        return assignment
    }

    // ─── Internal helpers ────────────────────────────────────────

    /**
     * Cluster a list of float values into groups where each group's values
     * are within [EDGE_CLUSTER_TOLERANCE] of each other. Returns the median
     * of each cluster, sorted ascending.
     */
    private fun clusterValues(values: List<Float>): List<Float> {
        if (values.isEmpty()) return emptyList()
        val sorted = values.sorted()
        val clusters = mutableListOf<MutableList<Float>>()

        for (v in sorted) {
            val lastCluster = clusters.lastOrNull()
            if (lastCluster != null && Math.abs(v - lastCluster.last()) < EDGE_CLUSTER_TOLERANCE) {
                lastCluster.add(v)
            } else {
                clusters.add(mutableListOf(v))
            }
        }

        // Use median of each cluster as the canonical edge value
        return clusters.map { cluster ->
            cluster.sorted()[cluster.size / 2]
        }
    }

    /**
     * Check whether a vertical divider exists at approximately x=[dividerX]
     * that spans through the vertical range [rowTop, rowBottom].
     *
     * A divider "exists" if there is at least one vertical line segment
     * near that X-coordinate whose Y-span covers at least 60% of the row height.
     */
    private fun hasVerticalDivider(
        vLines: List<PdfLineSegment>,
        dividerX: Float,
        rowTop: Float,
        rowBottom: Float
    ): Boolean {
        val rowHeight = rowBottom - rowTop
        if (rowHeight < 1f) return true // degenerate row

        // Find all V-line segments near this X
        val nearbySegments = vLines.filter {
            Math.abs(it.vX - dividerX) < EDGE_CLUSTER_TOLERANCE
        }

        // Check if any segment spans through this row's Y-range
        for (seg in nearbySegments) {
            val segTop = Math.min(seg.y0, seg.y1)
            val segBottom = Math.max(seg.y0, seg.y1)

            // Calculate overlap with the row's Y-range
            val overlapTop = Math.max(segTop, rowTop)
            val overlapBottom = Math.min(segBottom, rowBottom)
            val overlap = overlapBottom - overlapTop

            if (overlap >= rowHeight * 0.6f) {
                return true
            }
        }
        return false
    }

    /**
     * Check whether a horizontal divider exists at approximately y=[dividerY]
     * that spans through the horizontal range [colLeft, colRight].
     *
     * A divider "exists" if there is at least one horizontal line segment
     * near that Y-coordinate whose X-span covers at least 60% of the column width.
     */
    private fun hasHorizontalDivider(
        hLines: List<PdfLineSegment>,
        dividerY: Float,
        colLeft: Float,
        colRight: Float
    ): Boolean {
        val colWidth = colRight - colLeft
        if (colWidth < 1f) return true // degenerate column

        // Find all H-line segments near this Y
        val nearbySegments = hLines.filter {
            Math.abs(it.hY - dividerY) < EDGE_CLUSTER_TOLERANCE
        }

        // Check if any segment spans through this col's X-range
        for (seg in nearbySegments) {
            val segLeft = Math.min(seg.x0, seg.x1)
            val segRight = Math.max(seg.x0, seg.x1)

            // Calculate overlap with the col's X-range
            val overlapLeft = Math.max(segLeft, colLeft)
            val overlapRight = Math.min(segRight, colRight)
            val overlap = overlapRight - overlapLeft

            if (overlap >= colWidth * 0.6f) {
                return true
            }
        }
        return false
    }

    /**
     * Remove degenerate narrow spans from a sorted list of edge coordinates.
     * If two consecutive edges are closer than [minSpan], merge them by
     * keeping only the midpoint.
     */
    private fun filterDegenerateEdges(edges: List<Float>, minSpan: Float): List<Float> {
        if (edges.size < 2) return edges
        val result = mutableListOf(edges[0])
        var i = 1
        while (i < edges.size) {
            val prev = result.last()
            val curr = edges[i]
            if (curr - prev < minSpan) {
                // Degenerate span — replace previous with midpoint and skip current
                result[result.size - 1] = (prev + curr) / 2f
                i++
            } else {
                result.add(curr)
                i++
            }
        }
        return result
    }
}
