package com.company.qa.unified.pages.api;

import com.company.qa.unified.utils.Log;
import io.restassured.response.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * JobControlApi
 *
 * Encapsulates APIs for managing background / async jobs.
 *
 * Typical jobs:
 * - Data export (GDPR)
 * - Search reindexing
 * - Notification backfill
 * - Billing reconciliation
 * - Spam model refresh
 *
 * RULE:
 * ❌ Tests must NOT call RestAssured directly
 * ✅ Tests must ALWAYS use JobControlApi
 */
public class JobControlApi extends BaseApiClient {

    private static final Log log =
            Log.get(JobControlApi.class);

    /* =========================================================
       JOB TRIGGERING
       ========================================================= */

    /**
     * Trigger a background job.
     */
    public Response triggerJob(
            String jobName,
            Map<String, Object> parameters
    ) {

        log.info("⚙️ Triggering job={} params={}",
                jobName, parameters);

        Response response =
                post(
                        admin(),
                        "/admin/jobs/trigger",
                        Map.of(
                                "jobName", jobName,
                                "parameters", parameters
                        )
                );

        assertStatus(response, 202);
        return response;
    }

    /* =========================================================
       JOB STATUS
       ========================================================= */

    /**
     * Fetch job execution status.
     */
    public Response getJobStatus(String jobId) {

        log.info("📡 Fetching job status jobId={}", jobId);

        Response response =
                get(
                        admin(),
                        "/admin/jobs/" + jobId + "/status"
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       JOB CONTROL
       ========================================================= */

    /**
     * Cancel a running job.
     */
    public Response cancelJob(String jobId) {

        log.info("🛑 Cancelling job jobId={}", jobId);

        Response response =
                post(
                        admin(),
                        "/admin/jobs/" + jobId + "/cancel",
                        Map.of()
                );

        assertStatus(response, 200);
        return response;
    }

    /* =========================================================
       WAIT / POLLING HELPERS
       ========================================================= */

    /**
     * Wait until a job reaches a terminal state.
     */
    public JobResult waitForCompletion(
            String jobId,
            Duration timeout
    ) {

        Instant deadline = Instant.now().plus(timeout);

        log.info("⏳ Waiting for job={} completion (timeout={}s)",
                jobId, timeout.toSeconds());

        while (Instant.now().isBefore(deadline)) {

            Response statusResponse = getJobStatus(jobId);

            String status = statusResponse.path("status");

            if (isTerminal(status)) {

                log.info("✅ Job={} finished with status={}",
                        jobId, status);

                return new JobResult(
                        jobId,
                        status,
                        statusResponse.asString()
                );
            }

            sleep(2000);
        }

        fail("❌ Job did not complete within timeout: " + jobId);
        return null; // unreachable
    }

    private boolean isTerminal(String status) {
        return switch (status) {
            case "SUCCESS",
                 "FAILED",
                 "CANCELLED" -> true;
            default -> false;
        };
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    /* =========================================================
       RESULT MODEL
       ========================================================= */

    /**
     * Immutable job result model.
     */
    public static final class JobResult {

        private final String jobId;
        private final String status;
        private final String rawResponse;

        public JobResult(
                String jobId,
                String status,
                String rawResponse
        ) {
            this.jobId = jobId;
            this.status = status;
            this.rawResponse = rawResponse;
        }

        public String jobId() {
            return jobId;
        }

        public String status() {
            return status;
        }

        public String rawResponse() {
            return rawResponse;
        }

        public boolean isSuccess() {
            return "SUCCESS".equalsIgnoreCase(status);
        }

        public boolean isFailed() {
            return "FAILED".equalsIgnoreCase(status);
        }
    }
}
