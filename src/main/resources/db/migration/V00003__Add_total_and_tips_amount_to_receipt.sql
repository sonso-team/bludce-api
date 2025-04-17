-- Добавление полей для хранения суммы и чаевых
ALTER TABLE "receipt"
    ADD COLUMN "tips_value" NUMERIC(10, 2),
    ADD COLUMN "total_amount" NUMERIC(10, 2) NOT NULL default 0,
    ADD COLUMN "tips_amount" NUMERIC(10, 2) NOT NULL default 0;
