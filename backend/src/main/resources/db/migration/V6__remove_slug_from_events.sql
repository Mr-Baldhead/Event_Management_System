-- V6: Remove slug column from events table
-- Slug is no longer used in the application

ALTER TABLE events DROP INDEX slug;
ALTER TABLE events DROP COLUMN slug;
