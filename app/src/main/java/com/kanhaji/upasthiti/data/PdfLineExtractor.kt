package com.kanhaji.upasthiti.data

import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import com.tom_roush.pdfbox.contentstream.PDFGraphicsStreamEngine
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImage
import java.io.IOException

/**
 * A line segment extracted from the PDF content stream.
 * Coordinates are in PDF user space, converted to top-left origin
 * (Y increases downward, matching TextPosition.yDirAdj).
 */
data class PdfLineSegment(
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float
) {
    /** True if this segment is approximately horizontal (Y-difference < tolerance). */
    fun isHorizontal(tolerance: Float = 1.5f): Boolean =
        Math.abs(y0 - y1) < tolerance

    /** True if this segment is approximately vertical (X-difference < tolerance). */
    fun isVertical(tolerance: Float = 1.5f): Boolean =
        Math.abs(x0 - x1) < tolerance

    /** The constant Y-coordinate of a horizontal segment (average of endpoints). */
    val hY: Float get() = (y0 + y1) / 2f

    /** The constant X-coordinate of a vertical segment (average of endpoints). */
    val vX: Float get() = (x0 + x1) / 2f
}

/**
 * Container for all extracted grid lines from a PDF page.
 */
data class PdfGridLines(
    val horizontal: List<PdfLineSegment>,
    val vertical: List<PdfLineSegment>
)

/**
 * Extracts vector-drawn line segments from a PDF page's content stream.
 *
 * Works by intercepting `moveTo`, `lineTo`, `appendRectangle`, and
 * `strokePath`/`fillPath` operations from the PDF graphics pipeline.
 * Thin filled rectangles (common in table borders) are decomposed
 * into their constituent edge lines.
 *
 * Coordinates are transformed to a **top-left origin** system to match
 * the coordinate system used by PDFTextStripper's `yDirAdj`.
 */
class PdfLineExtractor(page: PDPage) : PDFGraphicsStreamEngine(page) {

    companion object {
        private const val TAG = "PdfLineExtractor"

        /**
         * Maximum width/height for a filled rectangle to be considered a
         * "line" rather than a shape. Table borders are typically ≤ 2pt thick.
         */
        private const val THIN_RECT_THRESHOLD = 3.0f
    }

    /** All horizontal and vertical line segments found on this page. */
    private val _segments = mutableListOf<PdfLineSegment>()

    /**
     * All subpaths being accumulated for the current path object.
     * Each subpath is a list of points from a moveTo through subsequent lineTo calls.
     * These are only committed to [_segments] when a paint operation occurs.
     */
    private val subPaths = mutableListOf<MutableList<PointF>>()

    /** The current subpath being built. */
    private var currentSubPath = mutableListOf<PointF>()

    /** Page height for Y-axis flip (PDF origin = bottom-left). */
    private val pageHeight: Float = page.mediaBox.height

    // ─── Public API ───────────────────────────────────────────────

    /**
     * Process the page and return all extracted grid lines, classified
     * into horizontal and vertical segments.
     */
    fun extract(): PdfGridLines {
        _segments.clear()
        processPage(page)

        val horizontal = _segments.filter { it.isHorizontal() }
        val vertical = _segments.filter { it.isVertical() }

        Log.d(TAG, "extract: Found ${horizontal.size} horizontal + ${vertical.size} vertical = ${_segments.size} total segments")
        return PdfGridLines(horizontal, vertical)
    }

    // ─── Path construction callbacks ──────────────────────────────

    @Throws(IOException::class)
    override fun moveTo(x: Float, y: Float) {
        // Start a new subpath. Save any existing subpath first.
        if (currentSubPath.isNotEmpty()) {
            subPaths.add(currentSubPath)
        }
        currentSubPath = mutableListOf(toPageSpace(x, y))
    }

    @Throws(IOException::class)
    override fun lineTo(x: Float, y: Float) {
        currentSubPath.add(toPageSpace(x, y))
    }

    @Throws(IOException::class)
    override fun curveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float) {
        // Curves aren't table grid lines — just track the endpoint so subsequent
        // lineTo/moveTo calls have the correct "current point".
        currentSubPath.add(toPageSpace(x3, y3))
    }

    @Throws(IOException::class)
    override fun closePath() {
        if (currentSubPath.size >= 2) {
            // Close the subpath by connecting last point back to first
            currentSubPath.add(PointF(currentSubPath[0].x, currentSubPath[0].y))
        }
    }

    // ─── Rectangle shorthand ─────────────────────────────────────

    @Throws(IOException::class)
    override fun appendRectangle(p0: PointF, p1: PointF, p2: PointF, p3: PointF) {
        // PDF `re` operator defines a rectangle as 4 corners.
        // Save current subpath, create a new one for the rectangle, close it.
        if (currentSubPath.isNotEmpty()) {
            subPaths.add(currentSubPath)
        }
        currentSubPath = mutableListOf(
            toPageSpaceFromPoint(p0),
            toPageSpaceFromPoint(p1),
            toPageSpaceFromPoint(p2),
            toPageSpaceFromPoint(p3),
            toPageSpaceFromPoint(p0) // close
        )
    }

    // ─── Paint operations ────────────────────────────────────────

    @Throws(IOException::class)
    override fun strokePath() {
        commitAllSubPathsAsLines()
    }

    @Throws(IOException::class)
    override fun fillPath(fillType: Path.FillType) {
        // Table borders in many PDFs are drawn as thin filled rectangles
        // (`re` + `f`) rather than stroked lines. Detect and decompose them.
        commitAllSubPathsAsFilledRects()
    }

    @Throws(IOException::class)
    override fun fillAndStrokePath(fillType: Path.FillType) {
        commitAllSubPathsAsLines()
    }

    @Throws(IOException::class)
    override fun endPath() {
        // Path ended without painting — discard it
        clearAllSubPaths()
    }

    // ─── Unused but required overrides ───────────────────────────

    @Throws(IOException::class)
    override fun clip(fillType: Path.FillType) { /* no-op */ }

    @Throws(IOException::class)
    override fun getCurrentPoint(): PointF {
        return if (currentSubPath.isNotEmpty()) currentSubPath.last()
        else PointF(0f, 0f)
    }

    @Throws(IOException::class)
    override fun drawImage(pdImage: PDImage?) { /* no-op */ }

    @Throws(IOException::class)
    override fun shadingFill(shadingName: COSName?) { /* no-op */ }

    // ─── Internal helpers ────────────────────────────────────────

    /**
     * Flip Y-axis: coordinates received by moveTo/lineTo/curveTo are already
     * CTM-transformed by PDFStreamEngine's operator handlers (transformedPoint).
     * We only need to convert from PDF bottom-left origin to top-left origin.
     */
    private fun toPageSpace(x: Float, y: Float): PointF {
        return PointF(x, pageHeight - y)
    }

    /**
     * Flip Y for PointF values received in appendRectangle
     * (also already CTM-transformed by the operator handler).
     */
    private fun toPageSpaceFromPoint(p: PointF): PointF =
        PointF(p.x, pageHeight - p.y)

    /**
     * Collect all pending subpaths into one list, then clear state.
     */
    private fun collectAllSubPaths(): List<List<PointF>> {
        if (currentSubPath.isNotEmpty()) {
            subPaths.add(currentSubPath)
        }
        val all = subPaths.toList()
        clearAllSubPaths()
        return all
    }

    private fun clearAllSubPaths() {
        subPaths.clear()
        currentSubPath = mutableListOf()
    }

    /**
     * Commit all pending subpaths as stroked lines.
     * Each pair of consecutive points in a subpath is a line segment.
     */
    private fun commitAllSubPathsAsLines() {
        val paths = collectAllSubPaths()
        for (subPath in paths) {
            if (subPath.size >= 2) {
                for (i in 0 until subPath.size - 1) {
                    val a = subPath[i]
                    val b = subPath[i + 1]
                    val seg = PdfLineSegment(a.x, a.y, b.x, b.y)
                    if (seg.isHorizontal() || seg.isVertical()) {
                        _segments.add(seg)
                    }
                }
            }
        }
    }

    /**
     * Commit all pending subpaths as filled rectangles.
     * If a subpath forms a thin rectangle, decompose it into a single line.
     * Otherwise, extract any horizontal/vertical edges from it.
     */
    private fun commitAllSubPathsAsFilledRects() {
        val paths = collectAllSubPaths()
        for (subPath in paths) {
            if (subPath.size >= 4) {
                val xs = subPath.map { it.x }
                val ys = subPath.map { it.y }
                val minX = xs.min()
                val maxX = xs.max()
                val minY = ys.min()
                val maxY = ys.max()
                val width = maxX - minX
                val height = maxY - minY

                if (width < THIN_RECT_THRESHOLD || height < THIN_RECT_THRESHOLD) {
                    // Thin rectangle → treat as a line
                    if (width < THIN_RECT_THRESHOLD && height >= THIN_RECT_THRESHOLD) {
                        // Vertical thin rect → vertical line at center X
                        val cx = (minX + maxX) / 2f
                        _segments.add(PdfLineSegment(cx, minY, cx, maxY))
                    } else if (height < THIN_RECT_THRESHOLD && width >= THIN_RECT_THRESHOLD) {
                        // Horizontal thin rect → horizontal line at center Y
                        val cy = (minY + maxY) / 2f
                        _segments.add(PdfLineSegment(minX, cy, maxX, cy))
                    }
                    // If both are thin, it's a dot — ignore
                }
                // Thick filled rectangle → not a grid line, ignore
            }
        }
    }
}
