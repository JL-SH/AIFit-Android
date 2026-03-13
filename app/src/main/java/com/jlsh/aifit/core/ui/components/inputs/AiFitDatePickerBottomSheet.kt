package com.jlsh.aifit.core.ui.components.inputs

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jlsh.aifit.core.ui.components.buttons.PrimaryButton
import com.jlsh.aifit.core.ui.theme.AIFitTheme
import com.jlsh.aifit.core.ui.theme.AiFitSpacing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private const val VISIBLE_ITEMS = 5
private val ITEM_HEIGHT = 44.dp
private val PICKER_HEIGHT = ITEM_HEIGHT * VISIBLE_ITEMS
private const val HALF_VISIBLE = VISIBLE_ITEMS / 2

// ─── Single Source of Truth ─────────────────────────────────────────────────

/**
 * Holds the single source of truth for the three date components.
 * [intendedDay] stores what the user actually picked (e.g. 31).
 * [effectiveDay] is clamped to the valid range for the current month/year.
 * When the user switches back to a month that supports 31 days, [intendedDay]
 * is restored automatically.
 */
@Stable
class DatePickerState(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
) {
    var year by mutableIntStateOf(initialYear)
        internal set

    var month by mutableIntStateOf(initialMonth)
        internal set

    /** The day the user intended to select (preserved across month changes). */
    var intendedDay by mutableIntStateOf(initialDay)
        internal set

    /** The actual valid day for the current month/year. */
    val effectiveDay: Int
        get() = intendedDay.coerceIn(1, maxDay)

    val maxDay: Int
        get() = DateValidator.daysInMonth(year, month)

    val validationResult: DateValidationResult
        get() = runCatching {
            DateValidator.validate(LocalDate.of(year, month, effectiveDay))
        }.getOrElse { DateValidationResult.FutureDate }

    val isoString: String
        get() = DateValidator.toIsoString(year, month, effectiveDay)

    companion object {
        val Saver: Saver<DatePickerState, List<Int>> = Saver(
            save = { listOf(it.year, it.month, it.intendedDay) },
            restore = { DatePickerState(it[0], it[1], it[2]) },
        )
    }
}

@Composable
private fun rememberDatePickerState(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
): DatePickerState = rememberSaveable(saver = DatePickerState.Saver) {
    DatePickerState(initialYear, initialMonth, initialDay)
}

// ─── Main Component ─────────────────────────────────────────────────────────

/**
 * Modal Bottom Sheet with scroll-wheel date pickers.
 *
 * @param isVisible       Controls visibility of the bottom sheet.
 * @param initialDate     Date to scroll to on open. Defaults to 2000-01-01 if null.
 * @param onDateSelected  Callback with the selected date in ISO-8601 (yyyy-MM-dd).
 * @param onDismiss       Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFitDatePickerBottomSheet(
    isVisible: Boolean,
    initialDate: String?,
    onDateSelected: (isoDate: String) -> Unit,
    onDismiss: () -> Unit,
) {
    if (!isVisible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val earliest = remember { DateValidator.earliestAllowedDate() }
    val latest = remember { DateValidator.latestAllowedDate() }
    val defaultDate = remember(initialDate) {
        val parsed = initialDate?.let { DateValidator.parseIsoString(it) }
        parsed?.coerceIn(earliest, latest) ?: LocalDate.of(2000, 1, 1)
    }

    val state = rememberDatePickerState(
        initialYear = defaultDate.year,
        initialMonth = defaultDate.monthValue,
        initialDay = defaultDate.dayOfMonth,
    )

    // Static lists (never change)
    val years = remember { (earliest.year..latest.year).toList() }
    val allMonths = remember { (1..12).toList() }

    // Day list derived from state
    val days = remember(state.maxDay) { (1..state.maxDay).toList() }

    val errorMessage = state.validationResult.toErrorMessage()

    // Locale-aware column order
    val locale = LocalConfiguration.current.locales[0]
    val columnOrder = remember(locale) { resolveColumnOrder(locale) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AiFitSpacing.md)
                .padding(bottom = AiFitSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Fecha de nacimiento",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(AiFitSpacing.sm))

            // Current selection preview
            val monthName = remember(state.month, locale) {
                Month.of(state.month).getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.uppercase() }
            }
            Text(
                text = "${state.effectiveDay} de $monthName de ${state.year}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
            )

            Spacer(Modifier.height(AiFitSpacing.md))

            // Scroll pickers
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PICKER_HEIGHT),
                contentAlignment = Alignment.Center,
            ) {
                // Selection band — independent composable, no state dependency
                SelectionBand()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    columnOrder.forEachIndexed { index, column ->
                        if (index > 0) Spacer(Modifier.width(AiFitSpacing.sm))

                        when (column) {
                            DateColumn.DAY -> {
                                ScrollWheelPicker(
                                    items = days,
                                    selectedIndex = state.effectiveDay - 1,
                                    onSettled = { idx -> state.intendedDay = days[idx] },
                                    label = { it.toString().padStart(2, '0') },
                                    modifier = Modifier.weight(0.8f),
                                )
                            }
                            DateColumn.MONTH -> {
                                ScrollWheelPicker(
                                    items = allMonths,
                                    selectedIndex = state.month - 1,
                                    onSettled = { idx -> state.month = allMonths[idx] },
                                    label = { monthIdx ->
                                        Month.of(monthIdx)
                                            .getDisplayName(TextStyle.SHORT, locale)
                                            .replaceFirstChar { it.uppercase() }
                                    },
                                    modifier = Modifier.weight(1.2f),
                                )
                            }
                            DateColumn.YEAR -> {
                                ScrollWheelPicker(
                                    items = years,
                                    selectedIndex = years.indexOf(state.year).coerceAtLeast(0),
                                    onSettled = { idx -> state.year = years[idx] },
                                    label = { it.toString() },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(AiFitSpacing.sm))

            // Error feedback
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = AiFitSpacing.xs),
                )
            }

            Spacer(Modifier.height(AiFitSpacing.md))

            PrimaryButton(
                text = "CONFIRMAR",
                onClick = { onDateSelected(state.isoString) },
                enabled = state.validationResult is DateValidationResult.Valid,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── Selection Band (independent, stateless) ────────────────────────────────

/**
 * Two horizontal lines marking the center selection zone.
 * Fully stateless — never recomposes due to scroll changes.
 */
@Composable
private fun SelectionBand() {
    val color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PICKER_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.height(ITEM_HEIGHT),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            HorizontalDivider(thickness = 1.dp, color = color)
            HorizontalDivider(thickness = 1.dp, color = color)
        }
    }
}

// ─── Scroll Wheel Picker ────────────────────────────────────────────────────

/**
 * A single scroll-wheel column.
 *
 * ## Architecture — Single Source of Truth from LazyListState
 *
 * The scroll position of [LazyListState] is the sole truth for what is
 * visually selected. State flows in ONE direction:
 *
 * ```
 * User scrolls → LazyListState updates → snapshotFlow detects settle →
 * onSettled(index) → parent updates selectedIndex → (no scroll triggered
 * because scroll is already at that position)
 * ```
 *
 * Programmatic scrolls (external [selectedIndex] change) are guarded by
 * a flag to prevent the settle detector from echoing the change back.
 *
 * ## Key design decisions
 *
 * 1. **Sub-pixel center calculation** via `firstVisibleItemScrollOffset`
 *    rounds to the nearest item, not just the first visible.
 * 2. **Spacer-aware indexing:** top/bottom padding items are factored into
 *    `computeCenteredDataIndex` so data indices are always correct.
 * 3. **No recomposition loop:** a programmatic-scroll flag is set before
 *    `scrollToItem` and cleared after settle, suppressing `onSettled`.
 * 4. **Visual highlight via `derivedStateOf`:** reacts directly to
 *    [LazyListState] scroll position, not external state.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T> ScrollWheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSettled: (index: Int) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val itemCount = items.size
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = selectedIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0)),
    )
    val itemHeightPx = with(LocalDensity.current) { ITEM_HEIGHT.toPx() }

    // Flag to suppress onSettled during programmatic scrolls.
    // Set BEFORE scrollToItem, cleared when settle is detected.
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // ── 1. Programmatic scroll (external selectedIndex changed) ──────────
    // Only fires when the user is NOT scrolling and position differs.
    // To center data[target], firstVisibleItemIndex must equal target
    // (spacer offset cancels out in computeCenteredDataIndex).
    LaunchedEffect(selectedIndex, itemCount) {
        val target = selectedIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        if (!listState.isScrollInProgress && listState.firstVisibleItemIndex != target) {
            isProgrammaticScroll = true
            listState.scrollToItem(target)
        }
    }

    // ── 2. Settle detection — the ONLY place that reports to parent ───────
    // Fires when isScrollInProgress transitions from true → false.
    // Reads the centered data index using sub-pixel offset for accuracy.
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .filter { !it } // only when scrolling STOPS
            .collectLatest {
                if (isProgrammaticScroll) {
                    // This settle was caused by our own scrollToItem — don't echo back.
                    isProgrammaticScroll = false
                    return@collectLatest
                }

                val centeredDataIndex = computeCenteredDataIndex(listState, itemHeightPx, itemCount)
                if (centeredDataIndex != selectedIndex) {
                    onSettled(centeredDataIndex)
                }
            }
    }

    // ── 3. Visual center — derived from scroll position, always in sync ──
    val visualCenteredDataIndex by remember {
        derivedStateOf {
            computeCenteredDataIndex(listState, itemHeightPx, itemCount)
        }
    }

    // ── 4. LazyColumn ────────────────────────────────────────────────────
    LazyColumn(
        state = listState,
        modifier = modifier.height(PICKER_HEIGHT),
        flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Top spacers (HALF_VISIBLE items so data(0) can be centered)
        items(HALF_VISIBLE) { spacerIdx ->
            Box(Modifier.height(ITEM_HEIGHT))
        }

        // Data items
        items(
            count = itemCount,
            key = { index -> "data_${itemCount}_$index" },
        ) { index ->
            val isCenter = index == visualCenteredDataIndex
            val distance = kotlin.math.abs(index - visualCenteredDataIndex)
            val alpha = when (distance) {
                0 -> 1f
                1 -> 0.55f
                else -> 0.3f
            }

            Box(
                modifier = Modifier
                    .height(ITEM_HEIGHT)
                    .fillMaxWidth()
                    .alpha(alpha),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label(items[index]),
                    style = if (isCenter) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.bodyLarge
                    },
                    color = if (isCenter) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bottom spacers
        items(HALF_VISIBLE) { spacerIdx ->
            Box(Modifier.height(ITEM_HEIGHT))
        }
    }
}

/**
 * Computes which data-item index is closest to the visual center of the picker.
 *
 * Layout model (all items same height):
 * ```
 * list index 0          → spacer 0         (top padding)
 * list index 1          → spacer 1         (top padding)
 * list index 2          → data item 0      ← HALF_VISIBLE offset
 * list index 3          → data item 1
 * ...
 * list index N+1        → data item N-1
 * list index N+2        → spacer 0         (bottom padding)
 * list index N+3        → spacer 1         (bottom padding)
 * ```
 *
 * The center of the viewport is at `firstVisibleItemIndex + HALF_VISIBLE`.
 * With sub-pixel offset: if more than half of the next item is showing,
 * we round up.
 */
private fun computeCenteredDataIndex(
    listState: LazyListState,
    itemHeightPx: Float,
    itemCount: Int,
): Int {
    val firstVisible = listState.firstVisibleItemIndex
    val scrollOffset = listState.firstVisibleItemScrollOffset

    // Round to nearest: if scroll offset > half item height, the next item
    // is more centered than the current firstVisible.
    val roundUp = if (itemHeightPx > 0f) (scrollOffset / itemHeightPx) else 0f
    val centeredListIndex = firstVisible + HALF_VISIBLE + if (roundUp >= 0.5f) 1 else 0

    // Convert from list-index space to data-index space (subtract spacer offset)
    val dataIndex = centeredListIndex - HALF_VISIBLE

    return dataIndex.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
}

// ─── Locale Column Order ────────────────────────────────────────────────────

private enum class DateColumn { DAY, MONTH, YEAR }

private fun resolveColumnOrder(locale: Locale): List<DateColumn> {
    val country = locale.country.uppercase()
    val mdyCountries = setOf("US", "PH", "FM", "MH", "PW")
    val ymdCountries = setOf("CN", "JP", "KR", "KP", "HU", "IR", "LT", "SE")
    return when (country) {
        in mdyCountries -> listOf(DateColumn.MONTH, DateColumn.DAY, DateColumn.YEAR)
        in ymdCountries -> listOf(DateColumn.YEAR, DateColumn.MONTH, DateColumn.DAY)
        else -> listOf(DateColumn.DAY, DateColumn.MONTH, DateColumn.YEAR)
    }
}

// ─── Preview ────────────────────────────────────────────────────────────────

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "DatePicker Dark",
)
@Composable
private fun AiFitDatePickerPreview() {
    AIFitTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Fecha de nacimiento",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "15 de Marzo de 1995",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                listOf("14", "15", "16").forEachIndexed { idx, day ->
                    val isSelected = idx == 1
                    Box(
                        modifier = Modifier
                            .height(ITEM_HEIGHT)
                            .width(60.dp)
                            .alpha(if (isSelected) 1f else 0.5f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = day,
                            style = if (isSelected) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            PrimaryButton(
                text = "CONFIRMAR",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
