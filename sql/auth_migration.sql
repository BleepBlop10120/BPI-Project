-- Run this AFTER schema.sql (adds login capability on top of existing accounts)
USE `project_banking_db`;

CREATE TABLE IF NOT EXISTS `account_credentials` (
  `credential_id`  bigint(20)   NOT NULL AUTO_INCREMENT,
  `account_number` varchar(20)  NOT NULL,
  `username`       varchar(50)  NOT NULL,
  `password_hash`  varchar(255) NOT NULL,
  `salt`           varchar(64)  NOT NULL,
  `created_at`     datetime     NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`credential_id`),
  UNIQUE KEY `uq_credentials_username` (`username`),
  UNIQUE KEY `uq_credentials_account` (`account_number`),
  CONSTRAINT `fk_credentials_account` FOREIGN KEY (`account_number`)
      REFERENCES `accounts` (`account_number`)
      ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
