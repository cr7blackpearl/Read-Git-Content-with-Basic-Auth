package com.gitrepositoryaccess.model;

import java.util.List;

public class GitHubList {
	List<String> equalData,deleteData,insertedData;
	
	
	public GitHubList() {
	}

	public GitHubList(List<String> equalData, List<String> deleteData, List<String> insertedData) {
		super();
		this.equalData = equalData;
		this.deleteData = deleteData;
		this.insertedData = insertedData;
	}

	public List<String> getEqualData() {
		return equalData;
	}

	public void setEqualData(List<String> equalData) {
		this.equalData = equalData;
	}

	public List<String> getDeleteData() {
		return deleteData;
	}

	public void setDeleteData(List<String> deleteData) {
		this.deleteData = deleteData;
	}

	public List<String> getInsertedData() {
		return insertedData;
	}

	public void setInsertedData(List<String> insertedData) {
		this.insertedData = insertedData;
	}
	
	
	

}
