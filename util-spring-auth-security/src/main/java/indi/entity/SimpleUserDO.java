package indi.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(name = "user")
public class SimpleUserDO {
	@Id private Long id;
	@Column private String password;
	@Column private String username;
	@Column private String avatar;
}

