-- DEMONSTRATION DATA ONLY. Import into a NEW, EMPTY ems_db schema.
-- Contains reusable demo account password hashes. Every account uses the demo password.
-- 14 employees, 14 user accounts, 8 time-off requests. Every employee has a login;
-- every employee except Taylor Admin (top of the organization) has an assigned manager.
-- Norma Wallis is DEACTIVATED and her login (normawallis1) is disabled.
CREATE DATABASE IF NOT EXISTS `ems_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `ems_db`;

-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: ems_db
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `employee`
--

-- Fresh database only: existing tables are not dropped.
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `employee` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `department` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `employment_status` varchar(255) DEFAULT NULL,
  `first_name` varchar(255) DEFAULT NULL,
  `job_title` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `manager_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'Operations','alex.profiletest@example.com','ACTIVE','Alex','Operations Specialist','Employee','555-0101',2),(2,'Operations','morgan.manager@example.com','ACTIVE','Morgan','Operations Manager','Manager','555-0102',3),(3,'Human Resources','taylor.admin@example.com','ACTIVE','Taylor','HR Administrator','Admin','555-0123',NULL),(4,'Operations','tabby.w@example.com','ACTIVE','Tabby','Operations Specialist','Watkins','555-555-5555',2),(5,'Operations','demonstration.e@example.com','ACTIVE','Demonstration','Operations Specialist','Employee','111-111-1111',2),(6,'Human Resources','jane.doe@company.com','ACTIVE','Jane','HR Administrator','Doe','3175550101',3),(7,'Finance','john.doe@company.com','ACTIVE','John','Accountant','Doe','3175550103',11),(8,'Engineering','tom.smith@company.com','ACTIVE','Tom','Developer','Smith','3175550103',9),(9,'Engineering','david.kim@company.com','ON_LEAVE','David','Engineering Manager','Kim','3175550104',3),(10,'Human Resources','imani.brooks@company.com','ACTIVE','Imani','HR Recruiter','Brooks','3175550105',6),(11,'Finance','fabian.ruiz@company.com','ACTIVE','Fabian','Finance Manager','Ruiz','3175550106',3),(12,'Finance','ella.brooks@company.com','ACTIVE','Ella','Accountant','Brooks','1234567890',11),(13,'Engineering','harry.butler@company.com','ACTIVE','Harry','Developer','Butler','1234657890',9),(14,'Human Resources','norma.wallis@company.com','DEACTIVATED','Norma','HR Recruiter','Wallis','1234562810',6);
/*!40000 ALTER TABLE `employee` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `time_off_request`
--

-- Fresh database only: existing tables are not dropped.
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `time_off_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `days_requested` decimal(38,2) DEFAULT NULL,
  `decided_at` datetime(6) DEFAULT NULL,
  `decision_note` varchar(1000) DEFAULT NULL,
  `end_date` date NOT NULL,
  `leave_type` varchar(255) NOT NULL,
  `reason` varchar(1000) DEFAULT NULL,
  `start_date` date NOT NULL,
  `status` varchar(255) NOT NULL,
  `submitted_at` datetime(6) DEFAULT NULL,
  `decided_by` bigint DEFAULT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1rfqu7gwwciamcw3qckryv5gp` (`decided_by`),
  KEY `FK1j0nkcyyhyqy6qdo37hf0fan3` (`employee_id`),
  CONSTRAINT `FK1j0nkcyyhyqy6qdo37hf0fan3` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`),
  CONSTRAINT `FK1rfqu7gwwciamcw3qckryv5gp` FOREIGN KEY (`decided_by`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_off_request`
--

LOCK TABLES `time_off_request` WRITE;
/*!40000 ALTER TABLE `time_off_request` DISABLE KEYS */;
INSERT INTO `time_off_request` VALUES (1,2.00,'2026-10-14 09:02:10.889267','Approved for testing manager authorization','2026-11-17','VACATION','Testing employee time-off ownership','2026-11-16','APPROVED','2026-10-14 08:29:47.222818',2,1),(2,1.00,'2026-10-14 09:08:45.323194','Concurrency test - denial','2026-12-01','PERSONAL','Testing department approval restrictions','2026-12-01','DENIED','2026-10-14 05:05:13.000000',3,3),(3,3.00,'2026-10-18 19:34:35.427428','Approved','2026-11-14','VACATION','Family vacation','2026-11-12','APPROVED','2026-10-18 19:31:10.015581',2,1),(4,3.00,'2026-10-18 23:05:18.304185','Demonstration','2026-10-31','VACATION','Demonstration','2026-10-29','APPROVED','2026-10-18 23:03:57.675732',2,1),(5,20.00,NULL,NULL,'2027-01-05','VACATION','christmas break','2026-12-17','PENDING','2026-10-10 18:41:54.737326',NULL,6),(6,32.00,NULL,NULL,'2027-01-01','SICK','christmas','2026-12-01','PENDING','2026-10-15 02:54:54.366438',NULL,6),(7,28.00,'2026-10-15 03:01:17.364334','','2027-02-01','VACATION','holiday','2027-01-05','APPROVED','2026-10-15 02:59:21.502287',11,6),(8,5.00,'2026-11-01 23:46:53.387084','happy new year!','2027-01-05','VACATION','new year trip','2027-01-01','APPROVED','2026-11-01 22:07:20.711912',6,8);
/*!40000 ALTER TABLE `time_off_request` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

-- Fresh database only: existing tables are not dropped.
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `is_enabled` bit(1) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` varchar(30) NOT NULL,
  `username` varchar(60) NOT NULL,
  `employee_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UKd1s31g1a7ilra77m65xmka3ei` (`employee_id`),
  CONSTRAINT `FKfndbe67uw6silwqnlyudtwqmo` FOREIGN KEY (`employee_id`) REFERENCES `employee` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','testemployee',1),(2,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','MANAGER','testmanager',2),(3,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','HR_ADMIN','testadmin',3),(4,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','HR_ADMIN','janedoe1',6),(5,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','MANAGER','fabianruiz',11),(6,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','johndoe',7),(7,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','tomsmith1',8),(8,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','MANAGER','davidkim1',9),(9,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','imanibrooks1',10),(10,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','ellabrooks1',12),(11,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','harrybutler1',13),(12,b'0','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','normawallis1',14),(13,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','tabbywatkins1',4),(14,b'1','$2a$10$MHFOcVg0DPmJh6k845ia7./KkFRroKGx6OQJ09EACbGIcSTxc/.5m','EMPLOYEE','demoemployee1',5);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-20 17:08:22
