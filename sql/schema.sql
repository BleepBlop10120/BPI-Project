-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 18, 2026 at 06:10 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `banking_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `accounts`
--

CREATE TABLE `accounts` (
  `account_id` bigint(20) NOT NULL COMMENT 'Surrogate primary key',
  `account_number` varchar(20) NOT NULL COMMENT 'Human-readable unique identifier (e.g. ACC-0001000001)',
  `account_name` varchar(100) NOT NULL COMMENT 'Full legal name of the account holder',
  `balance` decimal(15,2) NOT NULL DEFAULT 0.00 COMMENT 'Current available balance - always >= 0',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT 'Row creation timestamp (UTC)',
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Last modification timestamp (auto-updated)'
) ;

--
-- Dumping data for table `accounts`
--

INSERT INTO `accounts` (`account_id`, `account_number`, `account_name`, `balance`, `created_at`, `updated_at`) VALUES
(1, 'ACC-9961632275', 'Jake Arizo', 899476383.00, '2026-08-18 16:52:01', '2026-08-18 22:01:23'),
(2, 'ACC-5266428750', 'John Doe Doe', 586863.00, '2026-08-18 17:28:43', '2026-08-18 17:30:07'),
(3, 'ACC-3538344344', 'Jake', 50500.00, '2026-08-18 19:01:06', '2026-08-18 22:59:31'),
(4, 'ACC-7574505326', 'Jake Arizona', 5000000.00, '2026-08-18 21:26:05', '2026-08-18 21:26:05'),
(5, 'ACC-5815398383', 'John Lennon', 5000000.00, '2026-08-18 21:26:43', '2026-08-18 21:26:43'),
(6, 'ACC-4237293888', 'Jane Doe', 5004478.00, '2026-08-18 21:47:18', '2026-08-18 22:01:23'),
(7, 'ACC-7934744977', 'LeBron James', 49999940850.03, '2026-08-18 22:04:25', '2026-08-18 22:59:31'),
(8, 'ACC-8510506125', 'Bronny James', 10000000.00, '2026-08-18 22:47:24', '2026-08-18 22:47:24');

-- --------------------------------------------------------

--
-- Table structure for table `account_credentials`
--

CREATE TABLE `account_credentials` (
  `credential_id` bigint(20) NOT NULL,
  `account_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `salt` varchar(64) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `account_credentials`
--

INSERT INTO `account_credentials` (`credential_id`, `account_number`, `username`, `password_hash`, `salt`, `created_at`) VALUES
(1, 'ACC-9961632275', 'Jake', 'EJ1tz8YOSjr+WjpDzgS9o46GjS99hxVL4UPyd7+iUeM=', 'lNJlpZq/uJYLDMYOxQPNEQ==', '2026-08-18 18:46:20'),
(2, 'ACC-3538344344', 'Jake101', '27SjFSY35qYFmwuHFfrEfq2lAg91GnzDwfyKmchSfuY=', 'q8QL9wZTPO+8HfTww7Tiag==', '2026-08-18 19:01:06'),
(3, 'ACC-7574505326', 'jb98', 'yZ77njyNi/VWqC+zg9RqjIpuBzUtMGb++SAp7SSFkKU=', 'HrzsVll76iN6k2VR1jG0Cw==', '2026-08-18 21:26:06'),
(4, 'ACC-5815398383', 'JL', 'jqoyesezIq+QMKyFCQTAOOsaJ6SyZIFBYoydLqvKRfM=', '0pEFNKCOL+8zlfLtbfZdhw==', '2026-08-18 21:26:43'),
(5, 'ACC-4237293888', 'JD', 'ErFoSOdjV8IKJ6lpLzBE4DJg4Kvl046moyryvQ3AaG4=', 'Pr9O6oA7ine9PMbQTYhZSQ==', '2026-08-18 21:47:18'),
(6, 'ACC-7934744977', 'KingJ', 'q5uF1t4Lw9BOIBHYvsUzUNHgOQ3NFSscEvDEfVGCUVU=', '4k8+Eh+2lsbDpHxZG/bHag==', '2026-08-18 22:04:25'),
(7, 'ACC-8510506125', 'BJ', 'N3alW0E/FXWNI/i/WNevvrqrJgNsLOReZl5/8/UrlfY=', 'GK1QElF6TtLQ+nHRm1AUWA==', '2026-08-18 22:47:24');

-- --------------------------------------------------------

--
-- Table structure for table `transactions`
--

CREATE TABLE `transactions` (
  `transaction_id` bigint(20) NOT NULL COMMENT 'Surrogate primary key',
  `account_number` varchar(20) NOT NULL COMMENT 'Owning account (denormalised for query speed)',
  `transaction_type` enum('DEPOSIT','WITHDRAW','TRANSFER_IN','TRANSFER_OUT') NOT NULL COMMENT 'Category of financial event',
  `amount` decimal(15,2) NOT NULL COMMENT 'Absolute (positive) monetary amount',
  `balance_after` decimal(15,2) NOT NULL COMMENT 'Account balance snapshot immediately after this event',
  `reference_number` varchar(30) NOT NULL COMMENT 'Globally unique business reference (TXNyyyyMMddHHmmss + seq)',
  `remarks` varchar(255) DEFAULT NULL COMMENT 'Optional description - e.g. counterparty info for transfers',
  `created_at` datetime NOT NULL DEFAULT current_timestamp() COMMENT 'Event timestamp (UTC)'
) ;

--
-- Dumping data for table `transactions`
--

INSERT INTO `transactions` (`transaction_id`, `account_number`, `transaction_type`, `amount`, `balance_after`, `reference_number`, `remarks`, `created_at`) VALUES
(1, 'ACC-9961632275', 'WITHDRAW', 500.00, 899999500.00, 'TXN20260818165230000000001', 'Cash Withdrawal', '2026-08-18 16:52:30'),
(2, 'ACC-9961632275', 'DEPOSIT', 9.00, 899999509.00, 'TXN20260818165335000000001', 'Cash Deposit', '2026-08-18 16:53:35'),
(3, 'ACC-9961632275', 'TRANSFER_OUT', 523654.00, 899475855.00, 'TXN20260818172924000000001', 'Transfer to ACC-5266428750 (John Doe Doe)', '2026-08-18 17:29:24'),
(4, 'ACC-5266428750', 'TRANSFER_IN', 523654.00, 586869.00, 'TXN20260818172924000000002', 'Transfer from ACC-9961632275 (Jake Arizo)', '2026-08-18 17:29:24'),
(5, 'ACC-5266428750', 'TRANSFER_OUT', 6.00, 586863.00, 'TXN20260818173007000000003', 'Transfer to ACC-9961632275 (Jake Arizo)', '2026-08-18 17:30:07'),
(6, 'ACC-9961632275', 'TRANSFER_IN', 6.00, 899475861.00, 'TXN20260818173007000000004', 'Transfer from ACC-5266428750 (John Doe Doe)', '2026-08-18 17:30:07'),
(7, 'ACC-4237293888', 'DEPOSIT', 5000.00, 5005000.00, 'TXN20260818214800000000001', 'Cash Deposit', '2026-08-18 21:48:00'),
(8, 'ACC-4237293888', 'TRANSFER_OUT', 522.00, 5004478.00, 'TXN20260818220123000000001', 'Transfer to ACC-9961632275 (Jake Arizo)', '2026-08-18 22:01:23'),
(9, 'ACC-9961632275', 'TRANSFER_IN', 522.00, 899476383.00, 'TXN20260818220123000000002', 'Transfer from ACC-4237293888 (Jane Doe)', '2026-08-18 22:01:23'),
(10, 'ACC-7934744977', 'WITHDRAW', 5000.00, 49999995000.00, 'TXN20260818220514000000001', 'Cash Withdrawal', '2026-08-18 22:05:14'),
(11, 'ACC-7934744977', 'DEPOSIT', 50.00, 49999995050.00, 'TXN20260818220648000000001', 'Cash Deposit', '2026-08-18 22:06:48'),
(12, 'ACC-7934744977', 'WITHDRAW', 5000.00, 49999990050.00, 'TXN20260818220721000000002', 'Cash Withdrawal', '2026-08-18 22:07:21'),
(13, 'ACC-7934744977', 'DEPOSIT', 500.00, 49999990550.00, 'TXN20260818220741000000003', 'Cash Deposit', '2026-08-18 22:07:41'),
(14, 'ACC-7934744977', 'WITHDRAW', 50.00, 49999990500.00, 'TXN20260818221127000000001', 'Cash Withdrawal', '2026-08-18 22:11:27'),
(15, 'ACC-7934744977', 'DEPOSIT', 0.03, 49999990500.03, 'TXN20260818221253000000001', 'Cash Deposit', '2026-08-18 22:12:53'),
(16, 'ACC-7934744977', 'WITHDRAW', 1.00, 49999990499.03, 'TXN20260818221319000000002', 'Cash Withdrawal', '2026-08-18 22:13:19'),
(17, 'ACC-7934744977', 'DEPOSIT', 420.00, 49999990919.03, 'TXN20260818225628000000001', 'Cash Deposit', '2026-08-18 22:56:28'),
(18, 'ACC-7934744977', 'WITHDRAW', 69.00, 49999990850.03, 'TXN20260818225717000000002', 'Cash Withdrawal', '2026-08-18 22:57:17'),
(19, 'ACC-7934744977', 'TRANSFER_OUT', 50000.00, 49999940850.03, 'TXN20260818225931000000003', 'Transfer to ACC-3538344344 (Jake)', '2026-08-18 22:59:31'),
(20, 'ACC-3538344344', 'TRANSFER_IN', 50000.00, 50500.00, 'TXN20260818225931000000004', 'Transfer from ACC-7934744977 (LeBron James)', '2026-08-18 22:59:31');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `accounts`
--
ALTER TABLE `accounts`
  ADD PRIMARY KEY (`account_id`),
  ADD UNIQUE KEY `uq_account_number` (`account_number`),
  ADD KEY `idx_accounts_number` (`account_number`),
  ADD KEY `idx_accounts_created_at` (`created_at`);

--
-- Indexes for table `account_credentials`
--
ALTER TABLE `account_credentials`
  ADD PRIMARY KEY (`credential_id`),
  ADD UNIQUE KEY `uq_credentials_username` (`username`),
  ADD UNIQUE KEY `uq_credentials_account` (`account_number`);

--
-- Indexes for table `transactions`
--
ALTER TABLE `transactions`
  ADD PRIMARY KEY (`transaction_id`),
  ADD UNIQUE KEY `uq_reference_number` (`reference_number`),
  ADD KEY `idx_txn_account_created` (`account_number`,`created_at`),
  ADD KEY `idx_txn_reference_number` (`reference_number`),
  ADD KEY `idx_txn_type` (`transaction_type`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `accounts`
--
ALTER TABLE `accounts`
  MODIFY `account_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key';

--
-- AUTO_INCREMENT for table `account_credentials`
--
ALTER TABLE `account_credentials`
  MODIFY `credential_id` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `transactions`
--
ALTER TABLE `transactions`
  MODIFY `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Surrogate primary key';

--
-- Constraints for dumped tables
--

--
-- Constraints for table `account_credentials`
--
ALTER TABLE `account_credentials`
  ADD CONSTRAINT `fk_credentials_account` FOREIGN KEY (`account_number`) REFERENCES `accounts` (`account_number`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `transactions`
--
ALTER TABLE `transactions`
  ADD CONSTRAINT `fk_txn_account_number` FOREIGN KEY (`account_number`) REFERENCES `accounts` (`account_number`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
