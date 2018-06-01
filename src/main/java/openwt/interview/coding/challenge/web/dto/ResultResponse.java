package openwt.interview.coding.challenge.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
public class ResultResponse<T> {

	@JsonProperty("result")
	private T element;

	public ResultResponse() {

	}
	
	public ResultResponse(T element) {
		this.element = element; 
	}
	
}
