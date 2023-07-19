package com.davidlarocka.optimumcms.models;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ARTS")
@Data
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
	
	private String inputs;
	
   
    public void setCreatedCurrentEpoch() {
        this.created_at = Instant.now().getEpochSecond() ;
    }
    
    public void setUpdateCurrentEpoch() {
        this.updated_at = Instant.now().getEpochSecond() ;
    }
    
    public void setTsCurrentEpoch() {
        this.ts = Instant.now().getEpochSecond() ;
    }
    
    public long getTs() {
    	return ts;
    }

	
}
