-- DEMONSTRATION DATA ONLY. Import into a NEW, EMPTY ems_db schema.
-- Contains reusable demo account password hashes.
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
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `employee`
--

LOCK TABLES `employee` WRITE;
/*!40000 ALTER TABLE `employee` DISABLE KEYS */;
INSERT INTO `employee` VALUES (1,'Operations','alex.profiletest@example.com','ACTIVE','Alex','Operations Specialist','Employee','555-0101'),(2,'Operations','morgan.manager@example.com','ACTIVE','Morgan','Operations Manager','Manager','555-0102'),(3,'Human Resources','taylor.admin@example.com','ACTIVE','Taylor','HR Administrator','Admin','555-0123'),(4,'Operations','tabby.w@example.com','ACTIVE','Tabby','Operations Specialist','Watkins','555-555-5555'),(5,'Operations','demonstration.e@example.com','ACTIVE','Demonstration','Operations Specialist','Employee','111-111-1111');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `time_off_request`
--

LOCK TABLES `time_off_request` WRITE;
/*!40000 ALTER TABLE `time_off_request` DISABLE KEYS */;
INSERT INTO `time_off_request` VALUES (1,2.00,'2026-10-14 09:02:10.889267','Approved for testing manager authorization','2026-11-17','VACATION','Testing employee time-off ownership','2026-11-16','APPROVED','2026-10-14 08:29:47.222818',2,1),(2,1.00,'2026-10-14 09:08:45.323194','Concurrency test - denial','2026-12-01','PERSONAL','Testing department approval restrictions','2026-12-01','DENIED','2026-10-14 05:05:13.000000',3,3),(3,3.00,'2026-10-18 19:34:35.427428','Approved','2026-11-14','VACATION','Family vacation','2026-11-12','APPROVED','2026-10-18 19:31:10.015581',2,1),(4,3.00,'2026-10-18 23:05:18.304185','Demonstration','2026-10-31','VACATION','Demonstration','2026-10-29','APPROVED','2026-10-18 23:03:57.675732',2,1);
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,_binary '','$2a$10$ESNzXg8RVd087rTPyDH8WORH6AieluoftjlINhw6NzYtumL7dRQN.','EMPLOYEE','testemployee',1),(2,_binary '','$2a$10$ESNzXg8RVd087rTPyDH8WORH6AieluoftjlINhw6NzYtumL7dRQN.','MANAGER','testmanager',2),(3,_binary '','$2a$10$ESNzXg8RVd087rTPyDH8WORH6AieluoftjlINhw6NzYtumL7dRQN.','HR_ADMIN','testadmin',3);
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
