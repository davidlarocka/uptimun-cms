package com.davidlarocka.optimumcms.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidlarocka.optimumcms.models.Media;

public interface MediaStorageInterface extends JpaRepository<Media, Long> {
	

}
