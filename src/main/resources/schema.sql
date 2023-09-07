CREATE TABLE IF NOT EXISTS `index` (
       id INTEGER NOT NULL AUTO_INCREMENT,
       `rank` FLOAT NOT NULL,
       lemma_id INTEGER NOT NULL,
       page_id INTEGER NOT NULL,
       PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS lemma (
       id INTEGER NOT NULL AUTO_INCREMENT,
        frequency INTEGER NOT NULL,
        lemma VARCHAR(255) NOT NULL,
        site_id INTEGER NOT NULL,
        PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS `page` (
       id INTEGER NOT NULL AUTO_INCREMENT,
        code INT NOT NULL,
        content MEDIUMTEXT,
        path VARCHAR(500) NOT NULL,
        site_id INT NOT NULL,
        PRIMARY KEY (id)
    );

CREATE TABLE IF NOT EXISTS site (
       id INTEGER NOT NULL AUTO_INCREMENT,
        last_error TEXT,
        `name` VARCHAR(255) NOT NULL,
        status ENUM('INDEXING','INDEXED','FAILED') NOT NULL,
        status_time DATETIME NOT NULL,
        `url` VARCHAR(255) NOT NULL,
        PRIMARY KEY (id)
    );

--ALTER TABLE `page` ADD CONSTRAINT UKqwh8hnfgfsriqoq9xv4mrutdi UNIQUE (`path`, site_id);
--
--ALTER TABLE `index` ADD CONSTRAINT FKiqgm34dkvjdt7kobg71xlbr33 FOREIGN KEY (lemma_id) REFERENCES lemma (id);
--
--ALTER TABLE `index` ADD CONSTRAINT FKmhpxf442inrv8lxlmgrcn1x2v FOREIGN KEY (page_id) REFERENCES `page` (id) ON DELETE CASCADE;
--
--ALTER TABLE lemma ADD CONSTRAINT FKfbq251d28jauqlxirb1k2cjag FOREIGN KEY (site_id) REFERENCES site (id);
--
--ALTER TABLE `page` ADD CONSTRAINT FK2n838qvhuc5lxhvio71oafhfb FOREIGN KEY (site_id) REFERENCES site (id);