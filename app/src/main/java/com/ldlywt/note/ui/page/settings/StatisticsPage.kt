package com.ldlywt.note.ui.page.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ldlywt.note.R
import com.ldlywt.note.ui.page.LocalMemosState
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(UnstableSaltApi::class)
@Composable
fun StatisticsPage(navController: NavHostController) {
    val noteState = LocalMemosState.current
    val memos = noteState.notes

    val stats = remember(memos) {
        val totalNotes = memos.size
        val totalChars = memos.sumOf { (it.note.noteTitle?.length ?: 0) + it.note.content.length }
        val totalImages = memos.sumOf { it.note.attachments.size }

        val firstNoteTime = memos.minOfOrNull { it.note.createTime } ?: System.currentTimeMillis()
        val firstNoteDateStr = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault()).format(Date(firstNoteTime))
        val daysPassed = ((System.currentTimeMillis() - firstNoteTime) / (1000 * 60 * 60 * 24)).toInt()

        // Tags
        val tagMap = mutableMapOf<String, Int>()
        memos.forEach { bean ->
            bean.tagList.forEach { tag ->
                tagMap[tag.tag] = tagMap.getOrDefault(tag.tag, 0) + 1
            }
        }
        val topTags = tagMap.toList().sortedByDescending { it.second }.take(5)
        val totalTags = tagMap.size

        // Weekly Activity (Last 7 Days)
        val calendar = Calendar.getInstance()
        val weeklyActivity = mutableListOf<Pair<String, Int>>()
        val dayNotesMap = mutableMapOf<String, Int>()
        memos.forEach { bean ->
            val dayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(bean.note.createTime))
            dayNotesMap[dayKey] = dayNotesMap.getOrDefault(dayKey, 0) + 1
        }
        
        val tempCalendar = Calendar.getInstance()
        for (i in 0 until 7) {
            val dateLabel = SimpleDateFormat("M/d", Locale.getDefault()).format(tempCalendar.time)
            val dayKeyForMap = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCalendar.time)
            weeklyActivity.add(0, dateLabel to dayNotesMap.getOrDefault(dayKeyForMap, 0))
            tempCalendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Yearly Analysis (Current Year)
        val thisYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearlyMemos = memos.filter {
            calendar.timeInMillis = it.note.createTime
            calendar.get(Calendar.YEAR) == thisYear
        }
        
        val yearlyTotalNotes = yearlyMemos.size
        val yearlyMonthMap = mutableMapOf<Int, Int>()
        val yearlyDayMap = mutableMapOf<String, Int>()
        val yearlyDayCharMap = mutableMapOf<String, Int>()
        val hourMap = mutableMapOf<Int, Int>()
        var latestNoteTime: Long = 0
        var latestNightScore = -1
        
        var maxNoteChars = 0
        var minNoteChars = if (yearlyMemos.isNotEmpty()) Int.MAX_VALUE else 0


        yearlyMemos.forEach { bean ->
            val noteCharCount = (bean.note.noteTitle?.length ?: 0) + bean.note.content.length
            maxNoteChars = maxOf(maxNoteChars, noteCharCount)
            minNoteChars = minOf(minNoteChars, noteCharCount)

            calendar.timeInMillis = bean.note.createTime
            val month = calendar.get(Calendar.MONTH) + 1
            val dayKey = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(bean.note.createTime))
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            
            yearlyMonthMap[month] = yearlyMonthMap.getOrDefault(month, 0) + 1
            yearlyDayMap[dayKey] = yearlyDayMap.getOrDefault(dayKey, 0) + 1
            yearlyDayCharMap[dayKey] = yearlyDayCharMap.getOrDefault(dayKey, 0) + noteCharCount
            hourMap[hour] = hourMap.getOrDefault(hour, 0) + 1
            
            val score = if (hour < 6) hour + 24 else hour
            if (score >= 22 && score > latestNightScore) {
                latestNightScore = score
                latestNoteTime = bean.note.createTime
            }
        }
        
        val busiestMonth = yearlyMonthMap.maxByOrNull { it.value }?.key ?: -1
        val busiestDay = yearlyDayMap.maxByOrNull { it.value }?.key ?: "N/A"
        val maxDayNotes = yearlyDayMap.maxOfOrNull { it.value } ?: 0
        val maxDayChars = yearlyDayCharMap.maxOfOrNull { it.value } ?: 0
        val morningNotes = hourMap.filter { it.key in 5..11 }.values.sum()
        val afternoonNotes = hourMap.filter { it.key in 12..18 }.values.sum()
        val nightNotes = hourMap.filter { it.key in 19..23 || it.key in 0..4 }.values.sum()

        // Monthly Analysis (Current Month)
        val thisMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val monthlyMemos = yearlyMemos.filter {
            calendar.timeInMillis = it.note.createTime
            calendar.get(Calendar.MONTH) + 1 == thisMonth
        }
        val monthlyTotalNotes = monthlyMemos.size
        val monthlyCharCount = monthlyMemos.sumOf { (it.note.noteTitle?.length ?: 0) + it.note.content.length }
        val monthlyDayMap = mutableMapOf<String, Int>()
        val monthlyDayCharMap = mutableMapOf<String, Int>()
        val monthlyWordsMap = mutableMapOf<String, Int>()
        
        var monthlyMaxNoteChars = 0
        var monthlyMinNoteChars = if (monthlyMemos.isNotEmpty()) Int.MAX_VALUE else 0

        monthlyMemos.forEach { bean ->
            val noteCharCount = (bean.note.noteTitle?.length ?: 0) + bean.note.content.length
            monthlyMaxNoteChars = maxOf(monthlyMaxNoteChars, noteCharCount)
            monthlyMinNoteChars = minOf(monthlyMinNoteChars, noteCharCount)

            val dayKey = SimpleDateFormat("dd", Locale.getDefault()).format(Date(bean.note.createTime))
            monthlyDayMap[dayKey] = monthlyDayMap.getOrDefault(dayKey, 0) + 1
            monthlyDayCharMap[dayKey] = monthlyDayCharMap.getOrDefault(dayKey, 0) + noteCharCount
        }
        val monthlyBusiestDay = monthlyDayMap.maxByOrNull { it.value }?.key ?: "N/A"
        val monthlyMaxDayNotes = monthlyDayMap.maxOfOrNull { it.value } ?: 0
        val monthlyMaxDayChars = monthlyDayCharMap.maxOfOrNull { it.value } ?: 0
        val monthlyTopWords = monthlyWordsMap.toList().sortedByDescending { it.second }.take(3).map { it.first }

        NoteStats(
            totalNotes = totalNotes,
            totalChars = totalChars,
            totalTags = totalTags,
            totalImages = totalImages,
            topTags = topTags,
            weeklyActivity = weeklyActivity,
            yearlyStats = YearlyStats(
                year = thisYear,
                totalNotes = yearlyTotalNotes,
                activeDays = yearlyDayMap.size,
                maxNoteChars = maxNoteChars,
                minNoteChars = if (minNoteChars == Int.MAX_VALUE) 0 else minNoteChars,
                maxDayNotes = maxDayNotes,
                maxDayChars = maxDayChars,
                busiestMonth = busiestMonth,
                busiestDay = busiestDay,
                latestNoteTime = if (latestNoteTime > 0) SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(latestNoteTime)) else "N/A",
                morningNotes = morningNotes,
                afternoonNotes = afternoonNotes,
                nightNotes = nightNotes
            ),
            monthlyStats = MonthlyStats(
                month = thisMonth,
                totalNotes = monthlyTotalNotes,
                activeDays = monthlyDayMap.size,
                maxNoteChars = monthlyMaxNoteChars,
                minNoteChars = if (monthlyMinNoteChars == Int.MAX_VALUE) 0 else monthlyMinNoteChars,
                maxDayNotes = monthlyMaxDayNotes,
                maxDayChars = monthlyMaxDayChars,
                busiestDay = monthlyBusiestDay,
                topWords = monthlyTopWords,
                charCount = monthlyCharCount
            ),
            firstNoteDateStr = firstNoteDateStr,
            daysPassed = daysPassed
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .statusBarsPadding()
    ) {
        TitleBar(
            onBack = { navController.popBackStack() },
            text = stringResource(id = R.string.statistics)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                PoeticHeader(stats.firstNoteDateStr, stats.daysPassed)
            }
            item {
                OverviewSection(stats)
            }
            item {
                WeeklyActivitySection(stats.weeklyActivity)
            }
            item {
                MonthlyAnalysisSection(stats.monthlyStats)
            }

            item {
                YearlyAnalysisSection(stats.yearlyStats)
            }

            item {
                TagDistributionSection(stats.topTags)
            }

        }
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun PoeticHeader(firstNoteDateStr: String, daysPassed: Int) {
    RoundedColumn {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            val recordedDaysText = stringResource(id = R.string.stats_recorded_days, daysPassed.toString())
            val startIndex = recordedDaysText.indexOf(daysPassed.toString())
            
            Text(
                text = buildAnnotatedString {
                    if (startIndex != -1) {
                        append(recordedDaysText.substring(0, startIndex))
                        withStyle(SpanStyle(color = SaltTheme.colors.highlight, fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                            append(daysPassed.toString())
                        }
                        append(recordedDaysText.substring(startIndex + daysPassed.toString().length))
                    } else {
                        append(recordedDaysText)
                    }
                },
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = stringResource(id = R.string.stats_started_on, firstNoteDateStr),
                style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = SaltTheme.colors.text.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.stats_poetic_content),
                style = SaltTheme.textStyles.main.copy(
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = SaltTheme.colors.text.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic
                )
            )
        }
    }
}

data class NoteStats(
    val totalNotes: Int,
    val totalChars: Int,
    val totalTags: Int,
    val totalImages: Int,
    val topTags: List<Pair<String, Int>>,
    val weeklyActivity: List<Pair<String, Int>>,
    val yearlyStats: YearlyStats,
    val monthlyStats: MonthlyStats,
    val firstNoteDateStr: String,
    val daysPassed: Int
)

data class YearlyStats(
    val year: Int,
    val totalNotes: Int,
    val activeDays: Int,
    val maxNoteChars: Int,
    val minNoteChars: Int,
    val maxDayNotes: Int,
    val maxDayChars: Int,
    val busiestMonth: Int,
    val busiestDay: String,
    val latestNoteTime: String,
    val morningNotes: Int,
    val afternoonNotes: Int,
    val nightNotes: Int
)

data class MonthlyStats(
    val month: Int,
    val totalNotes: Int,
    val activeDays: Int,
    val maxNoteChars: Int,
    val minNoteChars: Int,
    val maxDayNotes: Int,
    val maxDayChars: Int,
    val busiestDay: String,
    val topWords: List<String>,
    val charCount: Int
)

@OptIn(UnstableSaltApi::class)
@Composable
fun OverviewSection(stats: NoteStats) {
    RoundedColumn {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = stringResource(R.string.all_note), value = stats.totalNotes.toString(), modifier = Modifier.weight(1f))
                StatItem(label = stringResource(R.string.characters), value = stats.totalChars.toString(), modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(label = stringResource(R.string.tag), value = stats.totalTags.toString(), modifier = Modifier.weight(1f))
                StatItem(label = stringResource(R.string.picture), value = stats.totalImages.toString(), modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun YearlyAnalysisSection(yearlyStats: YearlyStats) {
    RoundedColumn {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.stats_yearly_analysis, yearlyStats.year),
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            YearlyDetailItem(stringResource(id = R.string.stats_active_days), "${yearlyStats.activeDays} ${stringResource(id = R.string.dyas)}")
            YearlyDetailItem(stringResource(id = R.string.stats_total_records), stringResource(id = R.string.stats_records_unit, yearlyStats.totalNotes))
            YearlyDetailItem(stringResource(id = R.string.stats_max_day_records), "${stringResource(R.string.stats_records_unit, yearlyStats.maxDayNotes)} / ${stringResource(R.string.stats_chars_unit, yearlyStats.maxDayChars)}")
            YearlyDetailItem(stringResource(id = R.string.stats_note_length_extreme), stringResource(id = R.string.stats_max_min_length, yearlyStats.maxNoteChars, yearlyStats.minNoteChars))
            YearlyDetailItem(stringResource(id = R.string.stats_busiest_month), if (yearlyStats.busiestMonth > 0) stringResource(id = R.string.stats_monthly_summary, yearlyStats.busiestMonth).substringBefore(" ") else "N/A")
            YearlyDetailItem(stringResource(id = R.string.stats_busiest_day), yearlyStats.busiestDay)
            YearlyDetailItem(stringResource(id = R.string.stats_latest_note), yearlyStats.latestNoteTime)
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SaltTheme.colors.text.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.stats_work_rest_preference),
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HabitItem(stringResource(id = R.string.stats_morning_type), yearlyStats.morningNotes)
                HabitItem(stringResource(id = R.string.stats_afternoon_type), yearlyStats.afternoonNotes)
                HabitItem(stringResource(id = R.string.stats_night_type), yearlyStats.nightNotes)
            }
        }
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun MonthlyAnalysisSection(monthlyStats: MonthlyStats) {
    RoundedColumn {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.stats_monthly_summary, monthlyStats.month),
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = stringResource(id = R.string.stats_active_days), style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp))
                    Text(text = "${monthlyStats.activeDays} ${stringResource(id = R.string.dyas)}", style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(id = R.string.stats_this_month_records), style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp))
                    Text(text = stringResource(id = R.string.stats_records_unit, monthlyStats.totalNotes), style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = stringResource(id = R.string.stats_written_chars), style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp))
                    Text(text = stringResource(id = R.string.stats_chars_unit, monthlyStats.charCount), style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            YearlyDetailItem(stringResource(id = R.string.stats_max_day_records), "${stringResource(R.string.stats_records_unit, monthlyStats.maxDayNotes)} / ${stringResource(R.string.stats_chars_unit, monthlyStats.maxDayChars)}")
            YearlyDetailItem(stringResource(id = R.string.stats_note_length_extreme), stringResource(id = R.string.stats_max_min_length, monthlyStats.maxNoteChars, monthlyStats.minNoteChars))
            YearlyDetailItem(stringResource(id = R.string.stats_most_active_day), if (monthlyStats.busiestDay != "N/A") "${monthlyStats.busiestDay}${stringResource(R.string.day)}" else "N/A")
        }
    }
}

@Composable
private fun YearlyDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = SaltTheme.textStyles.sub)
        Text(text = value, style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Medium))
    }
}

@Composable
private fun HabitItem(label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp))
        Text(text = count.toString(), style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = SaltTheme.textStyles.main.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SaltTheme.colors.highlight
            )
        )
        Text(
            text = label,
            style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp, color = SaltTheme.colors.subText)
        )
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun TagDistributionSection(topTags: List<Pair<String, Int>>) {
    RoundedColumn {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.all_tag),
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (topTags.isEmpty()) {
                Text(text = "No tags yet", style = SaltTheme.textStyles.sub)
            } else {
                val maxCount = topTags.first().second
                topTags.forEach { (tag, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.width(80.dp),
                            maxLines = 1,
                            style = SaltTheme.textStyles.main.copy(fontSize = 14.sp)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SaltTheme.colors.highlight.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (maxCount > 0) count.toFloat() / maxCount else 0f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SaltTheme.colors.highlight)
                            )
                        }
                        Text(
                            text = count.toString(),
                            modifier = Modifier.width(40.dp),
                            style = SaltTheme.textStyles.sub.copy(fontSize = 12.sp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

@OptIn(UnstableSaltApi::class)
@Composable
fun WeeklyActivitySection(weeklyActivity: List<Pair<String, Int>>) {
    RoundedColumn {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.stats_recent_7_days),
                style = SaltTheme.textStyles.main.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp), // 增加一点高度
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxCount = weeklyActivity.maxOfOrNull { it.second } ?: 1
                weeklyActivity.forEach { (date, count) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (count > 0) {
                            Text(
                                text = count.toString(),
                                style = SaltTheme.textStyles.sub.copy(fontSize = 10.sp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .weight(1f, fill = false) // 使用 weight 自动分配剩余高度
                                .fillMaxHeight(if (maxCount > 0) (count.toFloat() / maxCount).coerceAtLeast(0.05f) else 0.05f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (count > 0) SaltTheme.colors.highlight else SaltTheme.colors.highlight.copy(alpha = 0.1f))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = date,
                            style = SaltTheme.textStyles.sub.copy(fontSize = 10.sp),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
