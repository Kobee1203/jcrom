package org.jcrom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.jcrom.annotations.JcrBaseVersionCreated;
import org.jcrom.annotations.JcrBaseVersionName;
import org.jcrom.annotations.JcrCheckedout;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jcrom.annotations.JcrVersionCreated;
import org.jcrom.annotations.JcrVersionName;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
@JcrNode(
	mixinTypes= {"mix:versionable"}
)
public class VersionedEntity {

	@JcrPath private String path;
	@JcrName private String name;
	@JcrProperty private String title;
	@JcrProperty private String body;
	
	@JcrBaseVersionName private String baseVersionName;
	@JcrBaseVersionCreated private Date baseVersionCreated;
	@JcrVersionName private String versionName;
	@JcrVersionCreated private Date versionCreated;
	
	@JcrCheckedout private boolean checkedOut;
    
    @JcrChildNode List<VersionedEntity> children;
    
    public VersionedEntity() {
        this.children = new ArrayList<VersionedEntity>();
    }

	public Date getBaseVersionCreated() {
		return baseVersionCreated;
	}

	public void setBaseVersionCreated(Date baseVersionCreated) {
		this.baseVersionCreated = baseVersionCreated;
	}

	public String getBaseVersionName() {
		return baseVersionName;
	}

	public void setBaseVersionName(String baseVersionName) {
		this.baseVersionName = baseVersionName;
	}

	public boolean isCheckedOut() {
		return checkedOut;
	}

	public void setCheckedOut(boolean checkedOut) {
		this.checkedOut = checkedOut;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		this.name = title;
	}

	public Date getVersionCreated() {
		return versionCreated;
	}

	public void setVersionCreated(Date versionCreated) {
		this.versionCreated = versionCreated;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

    public List<VersionedEntity> getChildren() {
        return children;
    }

    public void setChildren(List<VersionedEntity> children) {
        this.children = children;
    }
	
    public void addChild( VersionedEntity child ) {
        children.add(child);
    }
	
}
