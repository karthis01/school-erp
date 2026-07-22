package com.schoolerp.tenant;

/**
 * Holds the "current school" for the duration of one request/thread.
 *
 * Every request (except super-admin / school-management endpoints) belongs to exactly
 * one school. The school code is parsed from the "user@schoolcode" login username, embedded
 * in the JWT afterwards, and restored into this ThreadLocal on every request by
 * JwtAuthFilter so that the routing DataSource knows which physical database to talk to.
 *
 * IMPORTANT: because servlet containers use pooled threads, callers MUST always clear()
 * this in a finally block, otherwise a tenant could "leak" onto a thread that later serves
 * a different tenant's request.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_SCHOOL = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentSchool(String schoolCode) {
        CURRENT_SCHOOL.set(schoolCode == null ? null : schoolCode.toLowerCase());
    }

    public static String getCurrentSchool() {
        return CURRENT_SCHOOL.get();
    }

    public static boolean isSet() {
        return CURRENT_SCHOOL.get() != null;
    }

    public static void clear() {
        CURRENT_SCHOOL.remove();
    }
}
