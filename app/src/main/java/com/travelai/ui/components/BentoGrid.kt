package com.travelai.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Span options for [BentoGridScope.tile].
 *
 * Layout is 2 columns under the hood — [HalfWidth] occupies 1 col, [FullWidth]
 * occupies both, and the asymmetric values describe a 2/3 + 1/3 row split.
 *
 * Spans are resolved row-by-row: tiles are packed greedily into rows of total
 * width 1.0f, falling through to the next row when the running width can't
 * accommodate the next tile.
 */
enum class BentoSpan(internal val fraction: Float) {
    HalfWidth(0.5f),
    FullWidth(1.0f),
    TwoThirds(2f / 3f),
    OneThird(1f / 3f)
}

/**
 * DSL scope for [BentoGrid]. Inside the lambda, call [tile] to add cells.
 */
class BentoGridScope internal constructor() {
    internal val cells = mutableListOf<BentoCell>()

    fun tile(
        span: BentoSpan,
        height: Dp,
        content: @Composable () -> Unit
    ) {
        cells.add(BentoCell(span, height, content))
    }
}

internal data class BentoCell(
    val span: BentoSpan,
    val height: Dp,
    val content: @Composable () -> Unit
)

/**
 * Asymmetric two-column bento grid.
 *
 * Tiles are packed greedily into rows where running fractional width ≤ 1.0f.
 * When a tile would overflow, a new row starts. Row heights are independent
 * (each row uses the max of its tiles' heights).
 *
 * Example:
 * ```
 * BentoGrid {
 *   tile(BentoSpan.FullWidth, 180.dp) { HeroTile() }
 *   tile(BentoSpan.HalfWidth, 146.dp) { TileA() }
 *   tile(BentoSpan.HalfWidth, 146.dp) { TileB() }
 *   tile(BentoSpan.TwoThirds, 128.dp) { Wide() }
 *   tile(BentoSpan.OneThird, 128.dp) { Narrow() }
 * }
 * ```
 */
@Composable
fun BentoGrid(
    modifier: Modifier = Modifier,
    gap: Dp = 12.dp,
    content: BentoGridScope.() -> Unit
) {
    val scope = BentoGridScope().apply(content)
    val rows = packCellsIntoRows(scope.cells)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        rows.forEach { row ->
            val rowHeight = row.maxOf { it.height }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
                horizontalArrangement = Arrangement.spacedBy(gap)
            ) {
                row.forEach { cell ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .weight(cell.span.fraction)
                            .fillMaxWidth()
                    ) {
                        cell.content()
                    }
                }
            }
        }
    }
}

private fun packCellsIntoRows(cells: List<BentoCell>): List<List<BentoCell>> {
    val rows = mutableListOf<MutableList<BentoCell>>()
    var current = mutableListOf<BentoCell>()
    var widthUsed = 0f
    val epsilon = 0.001f

    cells.forEach { cell ->
        if (widthUsed + cell.span.fraction > 1f + epsilon) {
            rows.add(current)
            current = mutableListOf()
            widthUsed = 0f
        }
        current.add(cell)
        widthUsed += cell.span.fraction
        if (widthUsed >= 1f - epsilon) {
            rows.add(current)
            current = mutableListOf()
            widthUsed = 0f
        }
    }
    if (current.isNotEmpty()) rows.add(current)
    return rows
}
