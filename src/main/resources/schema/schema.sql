CREATE TABLE IF NOT EXISTS inb_transaction_status
  (
     id VARCHAR(36) NOT NULL,
     created_timestamp DATETIME NULL,
     last_updated_timestamp DATETIME NULL,
     created_by VARCHAR(50) NULL,
     updated_by VARCHAR(50) NULL,
     message TEXT NULL,
     file_name VARCHAR(1000) NULL,
     processed_count INT NOT NULL,
     total_count INT NOT NULL,
     interface_type VARCHAR(50) NOT NULL,
     source VARCHAR(50) NOT NULL,
     status VARCHAR(20) NOT NULL,
     CONSTRAINT pk_inb_transaction_status PRIMARY KEY (id)
  ) engine=innodb;

CREATE TABLE IF NOT EXISTS Custom_Prop_Config
(
id BIGINT auto_increment NOT NULL,
config_id VARCHAR(50) NULL,
property_key VARCHAR(50) NULL,
property_value VARCHAR(1000) NULL,
CONSTRAINT pk_Custom_Prop_Config PRIMARY KEY (id),
CONSTRAINT unique_ConfigId_Key UNIQUE (config_id, property_key)
) engine=innodb;

INSERT INTO Custom_Prop_Config (config_id,property_key, property_value)
SELECT * FROM (SELECT 'ASN', 'ActiveDCs', '991') AS tmp
WHERE NOT EXISTS (
    SELECT property_key FROM Custom_Prop_Config WHERE property_key = 'ActiveDCs'
) LIMIT 1;

INSERT INTO Custom_Prop_Config (config_id,property_key, property_value)
SELECT * FROM (SELECT 'ASN', 'EA', 'UNIT') AS tmp
WHERE NOT EXISTS (
    SELECT property_key FROM Custom_Prop_Config WHERE property_key = 'EA'
) LIMIT 1;
)
engine=innodb;

INSERT INTO Custom_Prop_Config (config_id,property_key, property_value)
SELECT * FROM (SELECT 'PO_Config', 'ActiveDCs', '990') AS tmp
WHERE NOT EXISTS (
    SELECT property_key FROM Custom_Prop_Config WHERE property_key = 'ActiveDCs'
) LIMIT 1;
INSERT INTO Custom_Prop_Config (config_id,property_key, property_value)
SELECT * FROM (SELECT 'PO_Config', 'EA', 'UNIT') AS tmp
WHERE NOT EXISTS (
    SELECT property_key FROM Custom_Prop_Config WHERE property_key = 'EA'
) LIMIT 1;

