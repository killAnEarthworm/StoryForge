-- Migration script to add missing updated_at columns
-- This fixes the schema validation error for tables that extend BaseEntity

-- Add updated_at to character_relationships
ALTER TABLE character_relationships
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at to timelines
ALTER TABLE timelines
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at to scenes
ALTER TABLE scenes
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at to dialogues
ALTER TABLE dialogues
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at to generation_history
ALTER TABLE generation_history
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add updated_at to character_memories
ALTER TABLE character_memories
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Verification query to check all tables now have updated_at
SELECT
    table_name,
    column_name,
    data_type,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public'
  AND column_name = 'updated_at'
ORDER BY table_name;
