package com.fauzan.budget.core;

import java.time.LocalDate;

public final class Checks {
    private Checks() {}
    public static void id(String id) {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,80}")) throw new IllegalArgumentException("ID tidak valid");
    }
    public static LocalDate date(String value) {
        if (value == null || !value.matches("\\d{4}-\\d{2}-\\d{2}")) throw new IllegalArgumentException("Tanggal harus YYYY-MM-DD");
        try { return LocalDate.parse(value); } catch (RuntimeException e) { throw new IllegalArgumentException("Tanggal tidak valid"); }
    }
    public static void account(String value) {
        if (!"KAS".equals(value) && !"PAGUDANA".equals(value)) throw new IllegalArgumentException("Akun tidak valid");
    }
    public static void money(long value) {
        if (value <= 0 || value > 1_000_000_000_000L) throw new IllegalArgumentException("Nominal harus 1–1.000.000.000.000 rupiah");
    }
    public static long balance(long current, long delta) {
        long next = Math.addExact(current, delta);
        if (next < 0) throw new IllegalArgumentException("Saldo tidak mencukupi");
        return next;
    }
}
