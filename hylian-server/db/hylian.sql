/*
 Navicat Premium Data Transfer

 Source Server         : frankcl的MySQL
 Source Server Type    : MySQL
 Source Server Version : 80030 (8.0.30)
 Source Host           : localhost:3306
 Source Schema         : hylian

 Target Server Type    : MySQL
 Target Server Version : 80030 (8.0.30)
 File Encoding         : 65001

 Date: 26/09/2024 14:12:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for active_record
-- ----------------------------
DROP TABLE IF EXISTS `active_record`;
CREATE TABLE `active_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `ticket_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_USER_ID` (`user_id`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_ID` (`id`) USING BTREE,
  KEY `INDEX_SESSION_ID` (`session_id`) USING BTREE,
  KEY `INDEX_TICKET_ID` (`ticket_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for app
-- ----------------------------
DROP TABLE IF EXISTS `app`;
CREATE TABLE `app` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `secret` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_APP_ID` (`id`) USING BTREE,
  KEY `INDEX_APP_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `resource` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_RESOURCE` (`resource`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission` (
  `id` bigint NOT NULL,
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `permission_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_ROLE_ID` (`role_id`) USING BTREE,
  KEY `INDEX_PERMISSION_ID` (`permission_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for tenant
-- ----------------------------
DROP TABLE IF EXISTS `tenant`;
CREATE TABLE `tenant` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `vendor_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_VENDOR_ID` (`vendor_id`) USING BTREE,
  KEY `INDEX_TENANT_ID` (`id`) USING BTREE,
  KEY `INDEX_TENANT_NAME` (`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `tenant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `vendor_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `INDEX_USER_NAME` (`user_name`) USING BTREE,
  KEY `INDEX_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_TENANT_ID` (`tenant_id`) USING BTREE,
  KEY `INDEX_VENDOR_ID` (`vendor_id`) USING BTREE,
  KEY `INDEX_USER_ID` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `role_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `app_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `INDEX_USER_ID` (`user_id`) USING BTREE,
  KEY `INDEX_ROLE_ID` (`role_id`) USING BTREE,
  KEY `INDEX_APP_ID` (`app_id`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1835699025827876867 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- Table structure for vendor
-- ----------------------------
DROP TABLE IF EXISTS `vendor`;
CREATE TABLE `vendor` (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `create_time` bigint NOT NULL,
  `update_time` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `INDEX_VENDOR_NAME` (`name`) USING BTREE,
  KEY `INDEX_CREATE_TIME` (`create_time`) USING BTREE,
  KEY `INDEX_UPDATE_TIME` (`update_time`) USING BTREE,
  KEY `INDEX_VENDOR_ID` (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

SET FOREIGN_KEY_CHECKS = 1;
