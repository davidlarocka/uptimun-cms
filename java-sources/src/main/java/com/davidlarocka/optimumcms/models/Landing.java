package com.davidlarocka.optimumcms.models;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name= "landing")
@Data
public class Landing {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id; 
	private String name;
	
	private long created_at;
	private long updated_at;
	private String template;
	private String type_url;
	private String url;
	private String file_url;
	private String areas;
	private String view;
	
	@Column(columnDefinition = "boolean default false")
	private boolean published;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getCreated_at() {
		return created_at;
	}
	public void setCreated_at(long created_at) {
		this.created_at = created_at;
	}
	public long getUpdated_at() {
		return updated_at;
	}
	
	public void setCreatedCurrentEpoch() {
        this.setCreated_at(Instant.now().getEpochSecond()) ;
    }
	
	public void setUpdateCurrentEpoch() {
	        this.setUpdated_at(Instant.now().getEpochSecond()) ;
	}
	 
	public void setUpdated_at(long updated_at) {
		this.updated_at = updated_at;
	}
	public String getTemplate() {
		return template;
	}
	public void setTemplate(String template) {
		this.template = template;
	}
	public String getType_url() {
		return type_url;
	}
	public void setType_url(String type_url) {
		this.type_url = type_url;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFile_url() {
		return file_url;
	}
	public void setFile_url(String file_url) {
		this.file_url = file_url;
	}
	public boolean isPublished() {
		return published;
	}
	public void setPublished(boolean published) {
		this.published = published;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAreas() {
		return areas;
	}
	public void setAreas(String areas) {
		this.areas = areas;
	}
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	
	
	
}
