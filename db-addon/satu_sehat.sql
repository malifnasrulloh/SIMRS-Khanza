CREATE TABLE satu_sehat_episode_of_care (
    id INT AUTO_INCREMENT PRIMARY KEY,
    no_rawat VARCHAR(20) NOT NULL,
    kd_penyakit VARCHAR(10) NOT NULL,
    status enum('Ralan','Ranap') NOT NULL,
    id_episode_of_care VARCHAR(100),

    INDEX idx_no_rawat (no_rawat),
    INDEX idx_kd_penyakit (kd_penyakit),
    INDEX idx_status (status),

    CONSTRAINT fk_episode_no_rawat 
        FOREIGN KEY (no_rawat) REFERENCES reg_periksa(no_rawat)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_episode_kd_penyakit 
        FOREIGN KEY (kd_penyakit) REFERENCES penyakit(kd_penyakit)
        ON DELETE CASCADE ON UPDATE CASCADE

) ENGINE=InnoDB 
DEFAULT CHARSET=latin1 
COLLATE=latin1_swedish_ci;
