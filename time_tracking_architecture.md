# Edit Echo - Time Tracking Architecture

## Design Philosophy
**Daily aggregation is the source of truth** - Roll up ephemeral session data into persistent daily records for accurate usage tracking and subscription enforcement.

## Data Model

### SessionLog (Ephemeral)
**Purpose**: Track individual recording sessions temporarily
**Lifecycle**: Created on recording end, wiped after daily rollup

```kotlin
@Entity(tableName = "session_log")
data class SessionLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,          // Epoch ms when session ended
    val durationMillis: Long,     // Recording duration in milliseconds
    val successful: Boolean = true // Only count if Whisper API succeeded
)
```

### DailyUsage (Persistent)
**Purpose**: One record per calendar day in user's local timezone
**Lifecycle**: Permanent storage for usage history and analytics

```kotlin
@Entity(tableName = "daily_usage", primaryKeys = ["localDate"])
data class DailyUsage(
    val localDate: String,        // ISO-8601 format "2025-05-23"
    val totalMillis: Long,        // Sum of successful session durations
    val sessionCount: Int,        // Number of successful sessions
    val createdAt: Long = System.currentTimeMillis()
)
```

## Usage Flow

### Recording Session End
1. **Calculate Duration**: `sessionEndTime - sessionStartTime`
2. **Check Success**: Only proceed if Whisper API call succeeded
3. **Insert SessionLog**: Store session with timestamp and duration
```kotlin
fun onRecordingComplete(durationMillis: Long, whisperSuccess: Boolean) {
    if (whisperSuccess) {
        sessionLogDao.insert(
            SessionLog(
                timestamp = System.currentTimeMillis(),
                durationMillis = durationMillis,
                successful = true
            )
        )
    }
}
```

### Daily Rollup Process
**Trigger**: App launch or date change detection
1. **Get Yesterday's Sessions**: Query SessionLog for previous day
2. **Aggregate Data**: Sum durations and count sessions
3. **Upsert DailyUsage**: Insert or update daily record
4. **Cleanup**: Delete processed SessionLog entries

```kotlin
fun performDailyRollup() {
    val yesterday = getYesterdayDateString()
    val sessions = sessionLogDao.getSessionsForDate(yesterday)
    
    if (sessions.isNotEmpty()) {
        val dailyUsage = DailyUsage(
            localDate = yesterday,
            totalMillis = sessions.sumOf { it.durationMillis },
            sessionCount = sessions.size
        )
        
        dailyUsageDao.upsert(dailyUsage)
        sessionLogDao.deleteProcessedSessions(sessions.map { it.id })
    }
}
```

## Usage Display & Limits

### Current Usage Calculation
```kotlin
fun getCurrentUsage(): UsageStats {
    val today = getTodayDateString()
    val todaySessions = sessionLogDao.getSessionsForDate(today)
    val todayMillis = todaySessions.sumOf { it.durationMillis }
    
    val lastWeekDaily = dailyUsageDao.getLastNDays(7)
    val weeklyMillis = lastWeekDaily.sumOf { it.totalMillis } + todayMillis
    
    val allTimeMillis = dailyUsageDao.getAllTimeTotal() + todayMillis
    
    return UsageStats(
        dailyMillis = todayMillis,
        weeklyMillis = weeklyMillis,
        allTimeMillis = allTimeMillis
    )
}
```

### UI Display Format
**Toggle Display**: User can cycle through D/WK/AT views
- **Daily**: "2h 15m / D" (today's usage)
- **Weekly**: "8h 30m / 12h WK" (used/limit this week)
- **All-Time**: "45h 22m / AT" (lifetime usage)

### Subscription Tier Enforcement
```kotlin
fun checkUsageLimit(tier: SubscriptionTier): UsageStatus {
    val weeklyUsage = getCurrentUsage().weeklyMillis
    val weeklyLimit = tier.weeklyLimitMillis
    
    return when {
        weeklyUsage >= weeklyLimit -> UsageStatus.LimitReached
        weeklyUsage >= (weeklyLimit * 0.8) -> UsageStatus.NearLimit
        else -> UsageStatus.WithinLimit
    }
}
```

## Benefits & Features

### Business Intelligence
- **Conversion Triggers**: Show usage approaching limits
- **Value Messaging**: "You've saved 7 hours this month"
- **Upgrade Prompts**: Smart timing based on usage patterns
- **Churn Prevention**: Identify low-usage users for engagement

### User Experience
- **Transparency**: Clear usage visibility builds trust
- **Flexibility**: Multiple time period views
- **Accuracy**: Only count successful API calls
- **Performance**: Minimal impact on app responsiveness

### Analytics Capabilities
- **Usage Patterns**: Peak usage times, session lengths
- **Retention Analysis**: Daily active patterns
- **Feature Adoption**: Tone preference trends
- **Revenue Optimization**: Tier utilization rates

## Implementation Considerations

### Data Integrity
- **Atomic Operations**: Rollup and cleanup in single transaction
- **Timezone Handling**: Use user's local timezone consistently
- **Clock Changes**: Handle daylight saving time transitions
- **Backup Strategy**: Daily usage data should be backed up

### Performance Optimization
- **Batch Operations**: Process multiple sessions at once
- **Background Processing**: Use WorkManager for rollup tasks
- **Database Indexes**: Optimize queries on date fields
- **Memory Management**: Limit in-memory session collection size

### Privacy & Security
- **Local Storage**: All data remains on device by default
- **Optional Sync**: Cloud backup with user consent only
- **Data Retention**: Configurable retention period for old data
- **User Control**: Export/delete usage data on request

## Future Enhancements
- **Smart Notifications**: Usage milestone celebrations
- **Predictive Limits**: Warn before hitting weekly limits
- **Usage Insights**: Personal productivity analytics
- **Team Features**: Shared usage pools for enterprise
- **API Integration**: Export to time tracking services