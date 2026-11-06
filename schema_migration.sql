-- ====================================================================
-- Wayfarer — Smart Travel Planner & Route Optimizer
-- Safe, Non-Destructive Database Migration Script
-- Target Database: smart_travel_planner
-- ====================================================================
-- This script enriches the existing database with a 'category' column
-- on the destinations table without modifying or deleting any existing
-- records, primary keys, or foreign key constraints.
-- ====================================================================

USE `smart_travel_planner`;

-- 1. Safely add 'category' column to 'destinations' if not already present
SET @col_exists = (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'smart_travel_planner'
      AND TABLE_NAME = 'destinations'
      AND COLUMN_NAME = 'category'
);

SET @stmt = IF(@col_exists = 0,
    'ALTER TABLE `destinations` ADD COLUMN `category` VARCHAR(50) NOT NULL DEFAULT \'Cultural\' AFTER `city`',
    'SELECT \'Column category already exists in destinations\' AS status'
);
PREPARE add_col_stmt FROM @stmt;
EXECUTE add_col_stmt;
DEALLOCATE PREPARE add_col_stmt;

-- 2. Populate meaningful categories based on destination names and descriptions
UPDATE `destinations` SET `category` = 'Adventure'
WHERE (`name` LIKE '%Trails%' OR `name` LIKE '%Trek%' OR `name` LIKE '%Mountain%' OR `description` LIKE '%adventure%')
  AND (`category` = 'Cultural' OR `category` IS NULL);

UPDATE `destinations` SET `category` = 'Relaxation'
WHERE (`name` LIKE '%Beach%' OR `name` LIKE '%Escape%' OR `name` LIKE '%Resort%' OR `name` LIKE '%Island%' OR `description` LIKE '%tropical%')
  AND (`category` = 'Cultural' OR `category` IS NULL);

UPDATE `destinations` SET `category` = 'Historical'
WHERE (`name` LIKE '%Temples%' OR `name` LIKE '%Shrine%' OR `name` LIKE '%Ancient%' OR `name` LIKE '%Art & Culture%' OR `description` LIKE '%historic%')
  AND (`category` = 'Cultural' OR `category` IS NULL);

UPDATE `destinations` SET `category` = 'Urban & Modern'
WHERE (`name` LIKE '%Skyscraper%' OR `city` = 'Tokyo' OR `name` LIKE '%Metropolis%')
  AND (`category` = 'Cultural' OR `category` IS NULL);

-- 3. Verify tables and counts
SELECT 'destinations' AS tbl, COUNT(*) AS total_rows FROM `destinations`
UNION ALL
SELECT 'travel_connections', COUNT(*) FROM `travel_connections`
UNION ALL
SELECT 'itineraries', COUNT(*) FROM `itineraries`
UNION ALL
SELECT 'itinerary_stops', COUNT(*) FROM `itinerary_stops`
UNION ALL
SELECT 'users', COUNT(*) FROM `users`;
