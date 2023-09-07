

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