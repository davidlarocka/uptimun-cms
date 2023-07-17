package com.davidlarocka.optimumcms.models;

import java.sql.Date;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ARTS")
@Getter
@Setter
public class Art {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id; 
	
	private String title;
	private long ts;
	private long created_at;
	private long updated_at;
	
	private String file;
	private String furl;
	private String type_url;
	
   
    public void setCreatedCurrentEpoch() {
        this.created_at = Instant.now().getEpochSecond() ;
    }
    
    public void setUpdateCurrentEpoch() {
        this.updated_at = Instant.now().getEpochSecond() ;
    }
    
    public void setTsCurrentEpoch() {
        this.ts = Instant.now().getEpochSecond() ;
    }

	
}
