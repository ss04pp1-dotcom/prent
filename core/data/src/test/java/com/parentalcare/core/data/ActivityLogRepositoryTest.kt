package com.parentalcare.core.data

import com.parentalcare.core.common.model.ActivityEvent
import com.parentalcare.core.common.model.EventType
import com.parentalcare.core.common.result.Result
import com.parentalcare.core.data.activity.ActivityLogRepository
import com.supabase.SupabaseClient
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert
import org.junit.Test

/**
 * Unit tests for ActivityLogRepository functionality.
 * Tests the data model and serialization without requiring Supabase connection.
 */
class ActivityLogRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testActivityEventSerialization() {
        val event = ActivityEvent(
            eventId = "evt_123",
            familyId = "fam_456",
            actorId = "user_789",
            actorType = ActivityEvent.ActorType.PARENT,
            type = EventType.SCREENSHOT_CAPTURED,
            targetDeviceId = "dev_123",
            requestId = "req_456",
            timestamp = System.currentTimeMillis(),
            message = "Screenshot captured for device dev_123"
        )

        val jsonString = json.encodeToString(event)
        val decoded = json.decodeFromString<ActivityEvent>(jsonString)

        Assert.assertEquals(event.eventId, decoded.eventId)
        Assert.assertEquals(event.familyId, decoded.familyId)
        Assert.assertEquals(event.actorId, decoded.actorId)
        Assert.assertEquals(event.actorType, decoded.actorType)
        Assert.assertEquals(event.type, decoded.type)
        Assert.assertEquals(event.targetDeviceId, decoded.targetDeviceId)
        Assert.assertEquals(event.requestId, decoded.requestId)
        Assert.assertEquals(event.message, decoded.message)
    }

    @Test
    fun testEventTypeEnumValues() {
        val allTypes = EventType.values()
        Assert.assertEquals(21, allTypes.size)
        
        Assert.assertTrue(allTypes.contains(EventType.SCREENSHOT_CAPTURED))
        Assert.assertTrue(allTypes.contains(EventType.REQUEST_SENT))
        Assert.assertTrue(allTypes.contains(EventType.PAIRING_CREATED))
        Assert.assertTrue(allTypes.contains(EventType.DEVICE_ONLINE))
        Assert.assertTrue(allTypes.contains(EventType.AUTH_EXPIRED))
    }

    @Test
    fun testActorTypeEnumValues() {
        val allTypes = ActivityEvent.ActorType.values()
        Assert.assertEquals(3, allTypes.size)
        
        Assert.assertTrue(allTypes.contains(ActivityEvent.ActorType.PARENT))
        Assert.assertTrue(allTypes.contains(ActivityEvent.ActorType.CHILD))
        Assert.assertTrue(allTypes.contains(ActivityEvent.ActorType.SYSTEM))
    }
}