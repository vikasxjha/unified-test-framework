package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * NotificationApi
 *
 * Encapsulates notification-related APIs.
 *
 * Covers:
 * - Fetching notifications (in-app)
 * - Marking read / unread
 * - Notification preferences
 * - Push notification triggers (test-only)
 * - Admin replay / resend
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use NotificationApi
 */
public class NotificationApi extends BaseApiClient {

    private static final Log log =
            Log.get(NotificationApi.class);

    /* =========================================================
       USER NOTIFICATIONS (IN-APP)
       ========================================================= */

    /**
     * Fetch notifications for the logged-in user.
     */
    public Response getNotifications(
            String accessToken,
            int limit,
            int offset
    ) {

        log.info("🔔 Fetching notifications limit={} offset={}",
                limit, offset);

        Response response =
                get(
                        authenticated(accessToken),
                        "/notifications?limit=" + limit + "&offset=" + offset
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Mark a notification as read.
     */
    public Response markAsRead(
            String accessToken,
            String notificationId
    ) {

        log.info("✅ Marking notification as read id={}",
                notificationId);

        Response response =
                post(
                        authenticated(accessToken),
                        "/notifications/" + notificationId + "/read",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Mark multiple notifications as read.
     */
    public Response markAllAsRead(
            String accessToken,
            List<String> notificationIds
    ) {

        log.info("✅ Marking {} notifications as read",
                notificationIds.size());

        Response response =
                post(
                        authenticated(accessToken),
                        "/notifications/read/bulk",
                        Map.of("notificationIds", notificationIds)
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       NOTIFICATION PREFERENCES
       ========================================================= */

    /**
     * Fetch notification preferences.
     */
    public Response getPreferences(String accessToken) {

        log.info("⚙️ Fetching notification preferences");

        Response response =
                get(
                        authenticated(accessToken),
                        "/notifications/preferences"
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Update notification preferences.
     */
    public Response updatePreferences(
            String accessToken,
            Map<String, Object> preferences
    ) {

        log.info("⚙️ Updating notification preferences");

        Response response =
                put(
                        authenticated(accessToken),
                        "/notifications/preferences",
                        preferences
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       PUSH NOTIFICATIONS (TEST / INTERNAL)
       ========================================================= */

    /**
     * Trigger a push notification (non-prod only).
     *
     * Used for E2E / realtime notification tests.
     */
    public Response triggerTestPush(
            String accessToken,
            String title,
            String body
    ) {

        log.info("📲 Triggering test push notification");

        Response response =
                post(
                        authenticated(accessToken),
                        "/notifications/test/push",
                        Map.of(
                                "title", title,
                                "body", body
                        )
                );

        assertStatus(response, 202);
        return response;
    }

    /* =========================================================
       ADMIN / OPERATIONS
       ========================================================= */

    /**
     * Replay / resend a notification (admin-only).
     */
    public Response replayNotification(
            String notificationId
    ) {

        log.info("🛠 Replaying notification id={}",
                notificationId);

        Response response =
                post(
                        admin(),
                        "/admin/notifications/replay",
                        Map.of("notificationId", notificationId)
                );

        assertStatus(response, 200);
        return response;
    }

    /**
     * Fetch delivery status for a notification (admin-only).
     */
    public Response getDeliveryStatus(
            String notificationId
    ) {

        log.info("📡 Fetching notification delivery status id={}",
                notificationId);

        Response response =
                get(
                        admin(),
                        "/admin/notifications/" + notificationId + "/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       ASSERTION HELPERS
       ========================================================= */

    public static void assertHasUnread(Response response) {

        Integer unreadCount = response.path("unreadCount");
        if (unreadCount == null || unreadCount <= 0) {
            fail("Expected unread notifications but found none");
        }
    }

    public static void assertNotificationPresent(
            Response response,
            String expectedTitle
    ) {

        List<Map<String, Object>> notifications =
                response.path("notifications");

        boolean found =
                notifications.stream()
                        .anyMatch(n ->
                                expectedTitle.equals(n.get("title")));

        if (!found) {
            fail("Notification with title '" +
                    expectedTitle + "' not found");
        }
    }

    public static void assertAllRead(Response response) {

        List<Map<String, Object>> notifications =
                response.path("notifications");

        boolean hasUnread =
                notifications.stream()
                        .anyMatch(n ->
                                Boolean.FALSE.equals(n.get("read")));

        if (hasUnread) {
            fail("Some notifications are still unread");
        }
    }
}
