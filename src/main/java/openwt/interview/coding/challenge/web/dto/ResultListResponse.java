package openwt.interview.coding.challenge.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultListResponse<T> {

	@JsonProperty("result_list")
	private List<T> resultList = new ArrayList<>();

	public ResultListResponse() {

	}
	
	public ResultListResponse(List<T> list) {
		for (T element : list)
			resultList.add(element);
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}
	
}
