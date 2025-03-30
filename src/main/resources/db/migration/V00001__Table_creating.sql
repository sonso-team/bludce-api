CREATE TABLE IF NOT EXISTS "users" (
    "id" uuid NOT NULL UNIQUE,
    "name" varchar(50) NOT NULL,
    "email" varchar(100) NOT NULL UNIQUE,
    "phone_number" varchar(11) NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

CREATE TABLE IF NOT EXISTS "passwords" (
    "id" uuid NOT NULL UNIQUE,
    "user_id" uuid NOT NULL,
    "passcode" varchar(6) NOT NULL,
    "expire_date" bigint NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "passwords_fk1" FOREIGN KEY ("user_id") REFERENCES "users"("id")
);
