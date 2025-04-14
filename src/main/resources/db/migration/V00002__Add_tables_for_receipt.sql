CREATE TABLE IF NOT EXISTS "receipt" (
    "id" uuid,
    "initiator_id" uuid NOT NULL,
    "receipt_type" varchar(20) NOT NULL,
    "tips_type" varchar(20) NOT NULL,
    "tips_percent" INT,
    "person_count" INT,
    "created_at" TIMESTAMP NOT NULL,
    "updated_at" TIMESTAMP,
    PRIMARY KEY ("id"),
    CONSTRAINT "receipt_fk1" FOREIGN KEY ("initiator_id") REFERENCES "users"("id")
);

CREATE TABLE IF NOT EXISTS "receipt_position" (
    "id" uuid,
    "receipt_id" uuid NOT NULL,
    "name" varchar(40) NOT NULL,
    "quantity" INT NOT NULL,
    "price" DECIMAL NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "receipt_position_fk1" FOREIGN KEY ("receipt_id") REFERENCES "receipt"("id")
);