package com.davidlarocka.optimumcms.interfaces;

import org.springframework.data.jpa.repository.JpaRepository;

import com.davidlarocka.optimumcms.models.Art;

public interface ArtInterface extends JpaRepository<Art, Long>{

}
