package com.lowcode.pipeline.ui.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    private String conversationID;
    private String message;
    private ChatType type;
	public String getConversationID() {
		return conversationID;
	}
	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public ChatType getType() {
		return type;
	}
	public void setType(ChatType type) {
		this.type = type;
	}
    
    
}
