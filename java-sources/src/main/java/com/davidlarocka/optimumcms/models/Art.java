package com.davidlarocka.optimumcms.models;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
	private String type_art;
	private String type_url;
	private String url;
	private String file_url;
	@Column(columnDefinition = "boolean default false")
	private boolean published;
	
	@Lob
	@Column( length = 100000 )
	private String inputs;//here the templates tags
	
	public void setPublished(boolean published) {
		this.published = published;
	}
	public boolean isPublished() {
		return published;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
   
    public void setCreatedCurrentEpoch() {
        this.setCreated_at(Instant.now().getEpochSecond()) ;
    }
    
    public void setUpdateCurrentEpoch() {
        this.setUpdated_at(Instant.now().getEpochSecond()) ;
    }
    
    public void setTsCurrentEpoch() {
        this.ts = Instant.now().getEpochSecond() ;
    }
    
    public void setTsCurrentDate() {
    	this.ts = Long.parseLong(DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())) ;
    }
    
    public long getTs() {
    	return ts;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public void setUpdated_at(long updated_at) {
		this.updated_at = updated_at;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getType_url() {
		return type_url;
	}

	public void setType_url(String type_url) {
		this.type_url = type_url;
	}

	public String getAllInfo (){
		return "{"+this.getInfoArtParamJson()  + inputs.substring(1);
	}
	
	public String getInputs (){
		return inputs;
	}

	public void setInputs(String inputs) {
		this.inputs = inputs;
	}

	public String getType_art() {
		return type_art;
	}

	public void setType_art(String type_art) {
		this.type_art = type_art;
	}
	
	private String getInfoArtParamJson() {
		return "\"_title\":\""+title+"\","+
			   "\"_url\":\""+url+"\","+
			   "\"_fileurl\":\""+file_url+"\","+
			   "\"_published\":\""+published+"\","+
			   "\"_datec\":\""+ String.valueOf(created_at) + " \" ,"+
			   "\"_datep\":\""+ String.valueOf(updated_at) +" \" ,"; 
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

	
}
