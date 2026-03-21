ALTER TABLE items DROP COLUMN contact_info;

ALTER TABLE items ADD COLUMN finder_name VARCHAR(100) NOT NULL;
ALTER TABLE items ADD COLUMN finder_email VARCHAR(150) NOT NULL;
ALTER TABLE items ADD COLUMN finder_college_id VARCHAR(8) NOT NULL CHECK (char_length(finder_college_id) = 8);

ALTER TABLE items ADD COLUMN claimer_name VARCHAR(100);
ALTER TABLE items ADD COLUMN claimer_email VARCHAR(150);
ALTER TABLE items ADD COLUMN claimer_college_id VARCHAR(8) CHECK (char_length(claimer_college_id) = 8);
