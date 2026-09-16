package com.fauzan.budget.core;

/** Immutable once queued: retries must retain id and all payload fields. */
public final class Command {
    public int schemaVersion = 1;
    public String id = "", kind = "", title = "", businessDate = "", account = "", periodId = "";
    public String eventId = "", targetId = "", receiptPath = "";
    public long amount, quantity, unitPrice, expectedRevision;

    public long validatedAmount() {
        if (schemaVersion != 1) throw new IllegalArgumentException("Versi data tidak didukung");
        Checks.id(id); Checks.id(periodId); Checks.date(businessDate);
        if (title == null || title.trim().isEmpty() || title.length() > 160) throw new IllegalArgumentException("Nama/alasan wajib diisi (maks. 160 karakter)");
        if (account == null || eventId == null || targetId == null || receiptPath == null) throw new IllegalArgumentException("Data tidak lengkap");
        if (!eventId.isEmpty()) Checks.id(eventId);
        if (!targetId.isEmpty()) Checks.id(targetId);
        if (receiptPath.length() > 300) throw new IllegalArgumentException("Referensi bukti tidak valid");
        switch (kind == null ? "" : kind) {
            case "EVENT_EDIT": case "EVENT_ARCHIVE": case "ASSET_ARCHIVE":
                Checks.id(targetId);
                if (expectedRevision < 1 || amount != 0 || quantity != 0 || unitPrice != 0 || !eventId.isEmpty() || !receiptPath.isEmpty() || !account.isEmpty())
                    throw new IllegalArgumentException("Perubahan informasi tidak valid");
                return 0;
            case "EVENT":
                if (amount != 0 || quantity != 0 || unitPrice != 0 || !eventId.isEmpty() || !targetId.isEmpty() || !receiptPath.isEmpty())
                    throw new IllegalArgumentException("Data kegiatan tidak valid");
                return 0;
            case "VOID":
                Checks.id(targetId); Checks.account(account);
                if (amount != 0 || quantity != 0 || unitPrice != 0 || !eventId.isEmpty() || !receiptPath.isEmpty()) throw new IllegalArgumentException("Pembatalan tidak valid");
                return 0;
            case "FUNDING":
                if (!"PAGUDANA".equals(account)) throw new IllegalArgumentException("Pagudana harus masuk akun PAGUDANA");
                break;
            case "CASH":
                if (!"KAS".equals(account)) throw new IllegalArgumentException("Iuran harus masuk akun KAS");
                break;
            case "EXPENSE":
                Checks.id(eventId); Checks.account(account);
                if (quantity < 1 || quantity > 1_000_000) throw new IllegalArgumentException("Jumlah item tidak valid");
                Checks.money(unitPrice);
                long total = Math.multiplyExact(quantity, unitPrice);
                Checks.money(total);
                if (amount != total) throw new IllegalArgumentException("Total tidak sesuai jumlah × harga");
                return -total;
            case "ASSET": Checks.account(account); break;
            default: throw new IllegalArgumentException("Jenis transaksi tidak valid");
        }
        if (!eventId.isEmpty() || quantity != 0 || unitPrice != 0) throw new IllegalArgumentException("Kolom tidak sesuai jenis transaksi");
        Checks.money(amount);
        return "ASSET".equals(kind) ? -amount : amount;
    }
}
